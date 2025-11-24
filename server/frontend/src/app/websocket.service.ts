import { Injectable } from '@angular/core';
import SockJS from 'sockjs-client';
import { Client, IMessage, Stomp } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private stompClient?: Client;
  private messageSubject = new BehaviorSubject<string | null>(null);
  public messages$ = this.messageSubject.asObservable();

  private WS_URL = 'http://localhost:8080/ws';

  connect(): void {
    const socket = new SockJS(this.WS_URL);
    this.stompClient = Stomp.over(socket);

    this.stompClient.debug = () => {};

    this.stompClient.activate();
    this.stompClient.onConnect = () => {
      console.log('✅ WebSocket connected');
        this.stompClient?.subscribe('/topic/logs', (message: IMessage) => {
          if (message.body) {
            this.messageSubject.next(message.body);
          }
        });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('❌ WebSocket error:', frame.headers['message']);
    };
  }

  disconnect(): void {
    this.stompClient?.deactivate();
    console.log('🔌 WebSocket disconnected');
  }
}