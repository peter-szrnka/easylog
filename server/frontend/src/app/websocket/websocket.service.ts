import { Injectable } from '@angular/core';
import { BehaviorSubject, ReplaySubject } from 'rxjs';
import { environment } from '../../environment/environment';
import { WebsocketState } from '../model';

/**
 * @author Peter Szrnka
 */
@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private wsClient?: WebSocket;
  private messageSubject = new BehaviorSubject<string | null>(null);
  private websocketSubject = new ReplaySubject<WebsocketState>(WebsocketState.LOADING);
  public messages$ = this.messageSubject.asObservable();
  public websocketState$ = this.websocketSubject.asObservable();

  connect(): void {
    this.websocketSubject.next(WebsocketState.LOADING);
    this.wsClient = new WebSocket(environment.webSocketUrl);
    this.wsClient.onopen = () => this.websocketSubject.next(WebsocketState.CONNECTED);
    this.wsClient.onmessage = (event) => this.messageSubject.next(event.data);
    this.wsClient.onerror = (event) => {
      this.websocketSubject.next(WebsocketState.FAILED);
      console.error(event);
      setTimeout(() => this.connect(), 10000);
    };
    this.wsClient.onclose = () => console.log("Disconnected");
  }

  disconnect(): void {
    this.wsClient?.close();
    console.log('WebSocket disconnected');
  }
}