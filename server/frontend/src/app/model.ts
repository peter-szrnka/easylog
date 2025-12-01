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
};

/**
 * @author Peter Szrnka
 */
export enum DateRangeType {
  CUSTOM = 'CUSTOM',
  LAST_5_MINUTES = 'LAST_5_MINUTES',
  LAST_15_MINUTES = 'LAST_15_MINUTES',
  LAST_30_MINUTES = 'LAST_30_MINUTES',
  LAST_1_HOUR = 'LAST_1_HOUR',
  LAST_4_HOURS = 'LAST_4_HOURS',
  LAST_1_DAY = 'LAST_1_DAY',
  LAST_7_DAYS = 'LAST_7_DAYS',
  LAST_1_MONTH = 'LAST_1_MONTH',
}

/**
 * @author Peter Szrnka
 */
export interface DateRangeSelection {
    dateRangeType: DateRangeType,
    from?: string,
    to?: string
}