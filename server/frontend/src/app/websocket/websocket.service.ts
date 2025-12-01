import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
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
  private stompClient?: Client;
  private messageSubject = new BehaviorSubject<string | null>(null);
  private websocketSubject = new ReplaySubject<WebsocketState>(WebsocketState.LOADING);
  public messages$ = this.messageSubject.asObservable();
  public websocketState$ = this.websocketSubject.asObservable();

  connect(): void {
    this.stompClient = new Client({
      brokerURL: environment.webSocketUrl,
      reconnectDelay: 10000
    });

    this.stompClient.debug = () => {};
    this.stompClient.activate();
    this.stompClient.onConnect = () => {
      this.websocketSubject.next(WebsocketState.CONNECTED);
      console.log('WebSocket connected');
        this.stompClient?.subscribe('/topic/logs', (message: IMessage) => {
          if (message.body) {
            this.messageSubject.next(message.body);
          }
        });
    };

    this.stompClient.onWebSocketError = (event) => {
      this.websocketSubject.next(WebsocketState.FAILED);
      console.error('WebSocket Error:', event);
    };
  }

  disconnect(): void {
    this.stompClient?.deactivate();
    console.log('WebSocket disconnected');
  }
}