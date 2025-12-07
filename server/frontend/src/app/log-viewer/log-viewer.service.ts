import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { DateRangeType, LogsResponse } from "../model";
import { environment } from "../../environment/environment";

/**
 * @author Peter Szrnka
 */
@Injectable({  providedIn: 'root' })
export class LogViewerService {
  
    constructor(private readonly httpClient: HttpClient) {}

    public list(
    dateRangeType: DateRangeType,
    filter?: string,
    startDate?: Date,
    endDate?: Date,
  
    page = 0,
    size = 20,
    sortBy = 'timestamp',
    sortDirection: 'asc' | 'desc' = 'desc',
  ): Observable<LogsResponse> {
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

    return this.httpClient.get<LogsResponse>(`${environment.apiUrl}/log`, { params });
  }
}