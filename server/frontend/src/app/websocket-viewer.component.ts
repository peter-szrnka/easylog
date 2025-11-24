import { Component, OnInit, OnDestroy, ChangeDetectorRef, CUSTOM_ELEMENTS_SCHEMA, ViewChild, AfterViewInit, ComponentRef } from '@angular/core';
import { debounceTime, fromEvent, map, Subscription } from 'rxjs';
import { WebsocketService } from './websocket.service';
import { LogEntry, LogEntryDisplayable, SaveLogRequest } from './model';
import { DatatableComponent, NgxDatatableModule } from '@swimlane/ngx-datatable';
import { LogService } from './log.service';
import { Title } from '@angular/platform-browser';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-websocket-viewer',
  templateUrl: './websocket-viewer.component.html',
  standalone: true,
  imports: [NgxDatatableModule, DatePipe, FormsModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  styleUrls: ['./websocket-viewer.component.scss'],
})
export class WebsocketViewerComponent
  implements OnInit, OnDestroy, AfterViewInit {
  messages: LogEntryDisplayable[] = [];
  private sub?: Subscription;
  private baseTitle = 'EasyLog Viewer';
  @ViewChild('search', { static: false }) search: any;
  @ViewChild('table') table!: DatatableComponent;

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

  constructor(
    private title: Title,
    private wsService: WebsocketService,
    private cd: ChangeDetectorRef,
    private logService: LogService
  ) { }

  ngOnInit(): void {
    this.title.setTitle(this.baseTitle);

    this.wsService.connect();
    this.sub = this.wsService.messages$.subscribe((msg: string | null) => {
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

  ngAfterViewInit(): void {
    fromEvent(this.search.nativeElement, 'keyup')
      .pipe(
        debounceTime(400),
        map((x) => ((x as any).target as HTMLInputElement).value)
      )
      .subscribe((value) => {
        this.filter = value;
        this.page = 0;
        this.loadLogs();
      });
  }

  loadLogs(): void {
    this.logService
      .list(
        this.filter,
        this.startDate ? new Date(this.startDate) : undefined,
        this.endDate ? new Date(this.endDate) : undefined,
        this.page,
        this.size,
        this.sortBy,
        this.sortDirection
      )
      .subscribe({
        next: (logs: any) => {
          if (logs.content) {
            // Page<LogEntry>
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