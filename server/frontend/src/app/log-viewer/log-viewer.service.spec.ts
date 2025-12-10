import { TestBed } from '@angular/core/testing';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { LogViewerService, ListLogsParams } from './log-viewer.service';
import { environment } from '../../environment/environment';
import { LogsResponse, DateRangeType } from '../model';
import { describe, it, beforeEach, expect, beforeAll } from 'vitest';
import { BrowserTestingModule, platformBrowserTesting } from '@angular/platform-browser/testing';
import { provideHttpClient } from '@angular/common/http';

/**
 * @author Peter Szrnka
 */
describe('LogViewerService', () => {
  let service: LogViewerService;
  let httpMock: HttpTestingController;

  beforeAll(() => {
    TestBed.initTestEnvironment(BrowserTestingModule, platformBrowserTesting());
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LogViewerService, provideHttpClient(), provideHttpClientTesting(),]
    });

    service = TestBed.inject(LogViewerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should call /log with all params', () => {
    const params: ListLogsParams = {
      page: 1,
      size: 10,
      sortBy: 'date',
      sortDirection: 'asc',
      dateRangeType: DateRangeType.LAST_1_DAY,
      filter: 'error',
      startDate: new Date('2025-01-01T00:00:00Z'),
      endDate: new Date('2025-01-02T00:00:00Z'),
    };

    const mockResponse: LogsResponse = { totalElements: 5, content: [] };

    service.list(params).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/log?page=1&size=10&sortBy=date&sortDirection=asc&dateRangeType=LAST_1_DAY&filter=error&startDate=2025-01-01T00:00:00.000Z&endDate=2025-01-02T00:00:00.000Z`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('sortBy')).toBe('date');
    expect(req.request.params.get('sortDirection')).toBe('asc');
    expect(req.request.params.get('dateRangeType')).toBe(DateRangeType.LAST_1_DAY);
    expect(req.request.params.get('filter')).toBe('error');
    expect(req.request.params.get('startDate')).toBe(params.startDate!.toISOString());
    expect(req.request.params.get('endDate')).toBe(params.endDate!.toISOString());

    req.flush(mockResponse);
  });

  it('should handle optional params correctly', () => {
    const params: ListLogsParams = {
      page: 0,
      size: 5,
      sortBy: 'id',
      sortDirection: 'desc',
      dateRangeType: DateRangeType.CUSTOM,
    };

    const mockResponse: LogsResponse = { totalElements: 0, content: [] };

    service.list(params).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/log?page=0&size=5&sortBy=id&sortDirection=desc&dateRangeType=CUSTOM`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.has('filter')).toBe(false);
    expect(req.request.params.has('startDate')).toBe(false);
    expect(req.request.params.has('endDate')).toBe(false);

    req.flush(mockResponse);
  });
});