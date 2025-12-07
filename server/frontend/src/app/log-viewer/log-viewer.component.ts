import { Component, OnInit, OnDestroy, ChangeDetectorRef, CUSTOM_ELEMENTS_SCHEMA, ViewChild, DestroyRef, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { WebsocketService } from '../websocket/websocket.service';
import { DateRangeSelection, DateRangeType, LogEntry, LogEntryDisplayable, LogsResponse, SaveLogRequest, WebsocketState } from '../model';
import { DatatableComponent, NgxDatatableModule, PageEvent } from '@swimlane/ngx-datatable';
import { LogViewerService } from './log-viewer.service';
import { Title } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DateRangeDropdownComponent } from '../date-range-dropdown/date-range-dropdown.component';
import { SpinnerComponent } from '../common/spinner.component';

/**
 * @author Peter Szrnka
 */
@Component({
  selector: 'app-log-viewer',
  templateUrl: './log-viewer.component.html',
  standalone: true,
  imports: [NgxDatatableModule, CommonModule, FormsModule, DateRangeDropdownComponent, SpinnerComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  styleUrls: ['./log-viewer.component.scss'],
})
export class LogViewerComponent
  implements OnInit, OnDestroy {
  private destroyRef = inject(DestroyRef);
  private title = inject(Title);
  private wsService = inject(WebsocketService);
  private cd = inject(ChangeDetectorRef);
  private logService = inject(LogViewerService);
  private route = inject(ActivatedRoute);

  messages: LogEntryDisplayable[] = [];
  private sub?: Subscription;
  private baseTitle = 'EasyLog Viewer';
  @ViewChild('search', { static: false }) search: any;
  @ViewChild('table') table!: DatatableComponent;
  websocketState: WebsocketState = WebsocketState.LOADING;
  loading = true;

  filter = '';
  startDate? = '';
  endDate? = '';
  page = 0;
  size = 50;
  sortBy = 'timestamp';
  sortDirection: 'asc' | 'desc' = 'desc';
  totalElements = 0;
  pageSizeOptions = [5, 10, 25, 50, 100];
  expandedRows: any[] = [];
  dateRangeType: DateRangeType = DateRangeType[(localStorage.getItem('range') || "") as keyof typeof DateRangeType];

  ngOnInit(): void {
    this.title.setTitle(this.baseTitle);
    this.wsService.websocketState$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((state) => {
      this.websocketState = state;
      this.cd.detectChanges();
    });

    this.wsService.connect();
    this.sub = this.wsService.messages$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((msg: string | null) => {
      if (msg) {
        const obj: SaveLogRequest = JSON.parse(msg) as SaveLogRequest;
        const mappedEntries = obj.entries.map((entry) => ({ ...entry, fromWebSocket: true })).sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
        this.messages = [
          ...mappedEntries,
          ...this.messages,
        ];
        this.totalElements += obj.entries.length;

        const fromWebSocketCount = this.messages.filter(
          (m) => m.fromWebSocket
        ).length;

        if (document.visibilityState === 'hidden') {
          this.title.setTitle(`${this.baseTitle} (${fromWebSocketCount})`);
        }
      }
      this.messages = [...this.messages];
      this.cd.detectChanges();
    });

     this.route.queryParams.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      this.filter = params['filter'] || '';
      this.startDate = params['startDate'] || '';
      this.endDate = params['endDate'] || '';
      this.page = params['page'] ? +params['page'] : 0;
      this.size = params['size'] ? +params['size'] : 50;
      this.sortBy = params['sortBy'] || 'timestamp';
      this.sortDirection = params['sortDirection'] || 'desc';
    });

    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        this.title.setTitle(this.baseTitle);
      }
    });
  }

  ngOnDestroy(): void {
    this.wsService.disconnect();
    this.sub?.unsubscribe();
  }

  fetchLogs(): void {
    this.page = 0;
    this.loadLogs();
  }

  onDateRangeChanged(event: DateRangeSelection) {
    this.startDate = event.from;
    this.endDate = event.to;
    this.dateRangeType = event.dateRangeType;
    localStorage.setItem('range', this.dateRangeType.toString());

    if (event.reloadLogs === true) {
      this.loadLogs();
    }
  }

  loadLogs(): void {
    this.loading = true;
    this.logService
      .list(
        this.dateRangeType,
        this.filter,
        this.dateRangeType === DateRangeType.CUSTOM ? (this.startDate ? new Date(this.startDate) : undefined) : undefined,
        this.dateRangeType === DateRangeType.CUSTOM ? (this.endDate ? new Date(this.endDate) : undefined) : undefined,
        this.page,
        this.size,
        this.sortBy,
        this.sortDirection,
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (logs: LogsResponse) => {
          this.messages = logs.content.map((log: LogEntry) => ({
            ...log,
            fromWebSocket: false,
          }));
          this.totalElements = logs.totalElements;
          this.loading = false;
          this.cd.detectChanges();
        },
        error: (err) => {
          console.error('Error:', err);
          this.loading = false;
          this.cd.detectChanges();
        },
      });
  }

  onPage(event: PageEvent): void {
    this.page = event.offset;
    this.loadLogs();
  }

  onSort(event: any): void {
    const sort = event.sorts[0];
    this.sortBy = sort.prop;
    this.sortDirection = sort.dir;
    this.loadLogs();
  }

  onActivate(event: any) {
    if (event.type === 'click' && event.row) {
      if (event.row.message && event.row.message.length > 100) {

        const index = this.expandedRows.indexOf(event.row);
        if (index > -1) {
          this.expandedRows.splice(index, 1);
        } else {
          this.expandedRows = [event.row];
        }
      }
      this.expandedRows = [...this.expandedRows];
      this.cd.detectChanges();
    }
  }

  getRowId(row: any): string {
    return row.messageId + row.timestamp + row.sessionId;
  }

  getRowClass = (row: LogEntryDisplayable) => ({
    'from-websocket': row.fromWebSocket,
  });
}