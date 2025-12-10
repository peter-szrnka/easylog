import { TestBed, ComponentFixture } from '@angular/core/testing';
import { LogViewerComponent } from './log-viewer.component';
import { WebsocketService } from '../websocket/websocket.service';
import { LogViewerService } from './log-viewer.service';
import { Title } from '@angular/platform-browser';
import { BehaviorSubject, of } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { describe, it, beforeEach, expect, vi, beforeAll } from 'vitest';
import { DateRangeType } from '../model';
import { provideZonelessChangeDetection } from '@angular/core';
import { BrowserTestingModule, platformBrowserTesting } from '@angular/platform-browser/testing';

class MockWebsocketService {
  websocketState$ = new BehaviorSubject(0);
  messages$ = new BehaviorSubject<string | null>(null);
  connect = vi.fn();
  disconnect = vi.fn();
}

class MockLogViewerService {
  list = vi.fn(() => of({ totalElements: 0, content: [] }));
}

class MockTitle {
  setTitle = vi.fn();
}

describe('LogViewerComponent', () => {
  let component: LogViewerComponent;
  let fixture: ComponentFixture<LogViewerComponent>;
  let wsService: MockWebsocketService;
  let logService: MockLogViewerService;
  let title: MockTitle;

  beforeAll(() => {
    TestBed.initTestEnvironment(BrowserTestingModule, platformBrowserTesting());
  });

  beforeEach(async () => {
    
    wsService = new MockWebsocketService();
    logService = new MockLogViewerService();
    title = new MockTitle();

    await TestBed.configureTestingModule({
      imports: [LogViewerComponent],
      providers: [
        { provide: WebsocketService, useValue: wsService },
        { provide: LogViewerService, useValue: logService },
        { provide: Title, useValue: title },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({}) }
        },
        provideZonelessChangeDetection()
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LogViewerComponent);
    component = fixture.componentInstance;
  });

  it('should set websocketState on websocket update', () => {
    const state = 1;
    wsService.websocketState$.next(state);
    fixture.detectChanges();
    expect(component.websocketState).toBe(state);
  });

  it('should add messages on websocket message', () => {
    const msg = JSON.stringify({
      entries: [{ messageId: '1', timestamp: '2025-01-01T00:00:00Z', sessionId: 's1', message: 'test' }]
    });
    wsService.messages$.next(msg);
    fixture.detectChanges();
    expect(component.messages.length).toBe(1);
    expect(component.totalElements).toBe(1);
  });

  it('should call wsService.connect on ngOnInit', () => {
    component.ngOnInit();
    expect(wsService.connect).toHaveBeenCalled();
  });

  it('should call wsService.disconnect on ngOnDestroy', () => {
    component.ngOnDestroy();
    expect(wsService.disconnect).toHaveBeenCalled();
  });

  it('should load logs using logService', () => {
    component.loadLogs();
    expect(logService.list).toHaveBeenCalled();
    expect(component.loading).toBe(false);
  });

  it('should reset page and fetch logs on fetchLogs', () => {
    const loadSpy = vi.spyOn(component, 'loadLogs');
    component.fetchLogs();
    expect(component.page).toBe(0);
    expect(loadSpy).toHaveBeenCalled();
  });

  it('should set page and call loadLogs on onPage', () => {
    const loadSpy = vi.spyOn(component, 'loadLogs');
    component.onPage({ offset: 2 } as any);
    expect(component.page).toBe(0);
    expect(loadSpy).toHaveBeenCalled();
  });

  it('should set sortBy and sortDirection on onSort', () => {
    const loadSpy = vi.spyOn(component, 'loadLogs');
    component.onSort({ sorts: [{ prop: 'message', dir: 'asc' }] });
    expect(component.sortBy).toBe('message');
    expect(component.sortDirection).toBe('asc');
    expect(loadSpy).toHaveBeenCalled();
  });

  it('should expand and collapse row onActivate', () => {
    component.expandedRows = [];
    const row = { messageId: '1', timestamp: 't', sessionId: 's', message: 'a'.repeat(101) };
    component.onActivate({ type: 'click', row } as any);
    expect(component.expandedRows.length).toBe(1);
    component.onActivate({ type: 'click', row } as any);
    expect(component.expandedRows.length).toBe(0);
  });

  it('should get row id', () => {
    const row = { messageId: '1', timestamp: 't', sessionId: 's' };
    expect(component.getRowId(row)).toBe('1ts');
  });

  it('should return correct row class', () => {
    const row = { fromWebSocket: true } as any;
    expect(component.getRowClass(row)['from-websocket']).toBe(true);
  });

  it('should compute getDateParam correctly', () => {
    component.dateRangeType = DateRangeType.LAST_15_MINUTES; 
    expect(component['getDateParam']('2025-01-01')).toBeUndefined();
    component.dateRangeType = DateRangeType.CUSTOM;
    expect(component['getDateParam']('2025-01-01')?.toISOString()).toBe(new Date('2025-01-01').toISOString());
    expect(component['getDateParam'](undefined)).toBeUndefined();
  });
});