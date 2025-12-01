import { HttpClient, HttpParams } from "@angular/common/http";
import { Inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { DateRangeType, LogEntry } from "../model";
import { environment } from "../../environment/environment";

/**
 * @author Peter Szrnka
 */
@Injectable({  providedIn: 'root' })
export class LogViewerService {
  
    constructor(private readonly httpClient: HttpClient) {}

    public list(
    filter?: string,
    startDate?: Date,
    endDate?: Date,
    dateRangeType: DateRangeType = DateRangeType.LAST_15_MINUTES,
    page: number = 0,
    size: number = 20,
    sortBy: string = 'timestamp',
    sortDirection: 'asc' | 'desc' = 'desc',
  ): Observable<LogEntry[]> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection)
      .set('dateRangeType', dateRangeType);

    if (filter) {
      params = params.set('filter', filter);
    }

    if (startDate) {
      params = params.set('startDate', startDate.toISOString());
    }

    if (endDate) {
      params = params.set('endDate', endDate.toISOString());
    }

    return this.httpClient.get<LogEntry[]>(`${environment.apiUrl}/log`, { params });
  }
}