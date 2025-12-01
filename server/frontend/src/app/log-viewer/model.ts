/**
 * @author Peter Szrnka
 */
export interface LogEntry {
    messageId: string,
    sessionId: string,
    logLevel: string,
    timestamp: string,
    tag: string,
    message: string,
    metadata: any;
};

/**
 * @author Peter Szrnka
 */
export interface LogEntryDisplayable extends LogEntry {
    fromWebSocket: boolean
};

/**
 * @author Peter Szrnka
 */
export interface SaveLogRequest {
    entries: LogEntry[]
};

/**
 * @author Peter Szrnka
 */
export enum WebsocketState {
    LOADING,
    FAILED,
    CONNECTED
}