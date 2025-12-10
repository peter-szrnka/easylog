import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { WebsocketService } from './websocket.service';
import { WebsocketState } from '../model';

describe('WebsocketService', () => {
    let service: WebsocketService;
    let mockWebSocketInstance: any;
    let originalWebSocket: any;

    beforeEach(() => {
        originalWebSocket = global.WebSocket;

        mockWebSocketInstance = {
            onopen: vi.fn(),
            onmessage: vi.fn(),
            onerror: vi.fn(),
            onclose: vi.fn(),
            close: vi.fn(),
        };

        class MockWebSocket {
            static CONNECTING = 0;
            static OPEN = 1;
            static CLOSING = 2;
            static CLOSED = 3;

            onopen: (() => void) | null = null;
            onmessage: ((event: { data: string }) => void) | null = null;
            onerror: ((event: any) => void) | null = null;
            onclose: (() => void) | null = null;

            constructor(url: string) {
                return mockWebSocketInstance;
            }

            close() {
                mockWebSocketInstance.close();
            }
        }

        global.WebSocket = MockWebSocket as unknown as typeof WebSocket;
        service = new WebsocketService();
    });

    afterEach(() => {
        global.WebSocket = originalWebSocket;
        vi.restoreAllMocks();
    });

    it('should initialize websocket and emit CONNECTED on open', () => {
        const stateSpy: WebsocketState[] = [];
        service.websocketState$.subscribe(state => stateSpy.push(state));

        service.connect();

        mockWebSocketInstance.onopen();

        expect(stateSpy).toContain(WebsocketState.LOADING);
        expect(stateSpy).toContain(WebsocketState.CONNECTED);
    });

    it('should emit messages when a message is received', () => {
        const messageSpy: (string | null)[] = [];
        service.messages$.subscribe(msg => messageSpy.push(msg));

        service.connect();

        const testMessage = 'Hello';
        mockWebSocketInstance.onmessage({ data: testMessage });

        expect(messageSpy).toContain(testMessage);
    });

    it('should emit FAILED and retry on error', async () => {
        vi.useFakeTimers();

        const stateSpy: WebsocketState[] = [];
        service.websocketState$.subscribe(state => stateSpy.push(state));

        service.connect();
        mockWebSocketInstance.onerror({ type: 'error' });

        expect(stateSpy).toContain(WebsocketState.FAILED);

        vi.advanceTimersByTime(10000);

        expect(stateSpy).toContain(WebsocketState.LOADING);

        vi.useRealTimers();
    });

    it('should close websocket on disconnect', () => {
        service.connect();
        service.disconnect();

        expect(mockWebSocketInstance.close).toHaveBeenCalledOnce();
    });
});
