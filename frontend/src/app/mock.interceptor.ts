import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpHandlerFn, HttpEventType, HttpResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, of, tap } from "rxjs";
import { environment } from "../environment/environment";


export function mockInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
    if (!environment.mock) {
        return next(req);
    }

    console.log("mockInterceptor intercepted request to:", req.url);

    return of(new HttpResponse({
        status: 200, body: [
            { "id": 1, "timestamp": "2024-06-01T12:00:00Z", "correlationId": "abc123", "sessionId": "sess1", "logLevel": "INFO", "tag": "INIT", "message": "Application started" },
            { "id": 2, "timestamp": "2024-06-01T12:05:00Z", "correlationId": "def456", "sessionId": "sess2", "logLevel": "ERROR", "tag": "DB", "message": "Database connection failed" }
        ]
    }));
}