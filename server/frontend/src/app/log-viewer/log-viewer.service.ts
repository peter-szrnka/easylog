import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable } from "rxjs";
import { DateRangeType, LogsResponse } from "../model";
import { environment } from "../../environment/environment";

export interface ListLogsParams {
  dateRangeType: DateRangeType;
  filter?: string;
  startDate?: Date;
  endDate?: Date;
  page: string | number | boolean;
  size: number;
  sortBy: string;
  sortDirection: 'asc' | 'desc';
}

/**
 * @author Peter Szrnka
 */
@Injectable({  providedIn: 'root' })
export class LogViewerService {
    private readonly httpClient = inject(HttpClient);


    public list(params: ListLogsParams): Observable<LogsResponse> {
    let httpParams = new HttpParams()
      .set('page', params.page)
      .set('size', params.size)
      .set('sortBy', params.sortBy)
      .set('sortDirection', params.sortDirection)
      .set('dateRangeType', params.dateRangeType);

    if (params.filter) {
      httpParams = httpParams.set('filter', params.filter!);
    }

    if (params.startDate) {
      httpParams = httpParams.set('startDate', params.startDate!.toISOString());
    }

    if (params.endDate) {
      httpParams = httpParams.set('endDate', params.endDate!.toISOString());
    }

    return this.httpClient.get<LogsResponse>(`${environment.apiUrl}/log`, { params: httpParams });
  }
}