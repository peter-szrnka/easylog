import { Component, OnInit, OnDestroy, ChangeDetectorRef, CUSTOM_ELEMENTS_SCHEMA, ViewChild, DestroyRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { WebsocketService } from '../websocket/websocket.service';
import { DateRangeSelection, DateRangeType, LogEntry, LogEntryDisplayable, SaveLogRequest, WebsocketState } from '../model';
import { DatatableComponent, NgxDatatableModule } from '@swimlane/ngx-datatable';
import { LogViewerService } from './log-viewer.service';
import { Title } from '@angular/platform-browser';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DateRangeDropdownComponent } from '../date-range-dropdown/date-range-dropdown.component';
import { SpinnerComponent } from '../common/spinner.component';

/**
 * @author Peter Szrnka
 */
@Component({
  selector: 'log-viewer',
  templateUrl: './log-viewer.component.html',
  standalone: true,
  imports: [NgxDatatableModule, DatePipe, CommonModule, FormsModule, DateRangeDropdownComponent, SpinnerComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  styleUrls: ['./log-viewer.component.scss'],
})
export class LogViewerComponent
  implements OnInit, OnDestroy {
  messages: LogEntryDisplayable[] = [];
  private sub?: Subscription;
  private baseTitle = 'EasyLog Viewer';
  @ViewChild('search', { static: false }) search: any;
  @ViewChild('table') table!: DatatableComponent;
  websocketState: WebsocketState = WebsocketState.LOADING;

  filter: string = '';
  startDate?: string = '';
  endDate?: string = '';
  page: number = 0;
  size: number = 50;
  sortBy: string = 'timestamp';
  sortDirection: 'asc' | 'desc' = 'desc';
  totalElements = 0;
  pageSizeOptions = [5, 10, 25, 50, 100];
  expandedRows: any[] = [];
  dateRangeType: DateRangeType = DateRangeType.LAST_15_MINUTES;

  constructor(
    private destroyRef: DestroyRef,
    private title: Title,
    private wsService: WebsocketService,
    private cd: ChangeDetectorRef,
    private logService: LogViewerService,
    private route: ActivatedRoute,
  ) { }

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
      this.totalElements = this.totalElements;
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

      this.loadLogs();
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

  onDateRangeChanged($event: DateRangeSelection) {
    this.startDate = $event.from;
    this.endDate = $event.to;
    this.dateRangeType = $event.dateRangeType;
  }

  loadLogs(): void {
    this.logService
      .list(
        this.filter,
        this.dateRangeType === DateRangeType.CUSTOM ? (this.startDate ? new Date(this.startDate) : undefined) : undefined,
        this.dateRangeType === DateRangeType.CUSTOM ? (this.endDate ? new Date(this.endDate) : undefined) : undefined,
        this.dateRangeType,
        this.page,
        this.size,
        this.sortBy,
        this.sortDirection,
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (logs: any) => {
          if (logs.content) {
            this.messages = logs.content.map((log: LogEntry) => ({
              ...log,
              fromWebSocket: false,
            }));
            this.totalElements = logs.totalElements;
          } else {
            this.messages = logs.map((log: LogEntry) => ({
              ...log,
              fromWebSocket: false,
            }));
          }
          this.cd.detectChanges();
        },
        error: (err) => console.error('Error:', err),
      });
  }

  onPage(event: any): void {
    this.page = event.offset;
    this.loadLogs();
  }

  onSort(event: any): void {
    const sort = event.sorts[0];
    this.sortBy = sort.prop;
    this.sortDirection = sort.dir;
    this.loadLogs();
  }

  onDateChange(): void {
    this.page = 0;
    this.loadLogs();
  }

  onPageSizeChange(event: any) {
    this.page = 0;
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
    return row.correlationId + row.timestamp + row.sessionId;
  }

  getRowClass = (row: LogEntryDisplayable) => ({
    'from-websocket': row.fromWebSocket,
  });
}