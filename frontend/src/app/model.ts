export interface LogEntry {
    messageId: string,
    correlationId: string,
    sessionId: string,
    logLevel: string,
    timestamp: string,
    tag: string,
    message: string,
    metadata: any;
};

export interface LogEntryDisplayable extends LogEntry {
    fromWebSocket: boolean
};


export interface SaveLogRequest {
    entries: LogEntry[]
};