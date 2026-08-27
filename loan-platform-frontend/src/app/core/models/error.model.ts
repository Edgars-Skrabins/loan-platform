export enum ErrorType {
  UNAUTHORIZED = 'UNAUTHORIZED',
  FORBIDDEN = 'FORBIDDEN',
  NOT_FOUND = 'NOT_FOUND',
  VALIDATION = 'VALIDATION',
  SERVER_ERROR = 'SERVER_ERROR',
  NETWORK_ERROR = 'NETWORK_ERROR',
  UNKNOWN = 'UNKNOWN'
}

export interface ApiError {
  type: ErrorType;
  message: string;
  statusCode: number;
  details?: Record<string, any>;
  timestamp?: string;
}

export class AppError implements ApiError {
  type: ErrorType;
  message: string;
  statusCode: number;
  details?: Record<string, any>;
  timestamp?: string;

  constructor(
    type: ErrorType,
    message: string,
    statusCode: number,
    details?: Record<string, any>
  ) {
    this.type = type;
    this.message = message;
    this.statusCode = statusCode;
    this.details = details;
    this.timestamp = new Date().toISOString();
  }

  static fromHttpError(error: any): AppError {
    if (!error.status) {
      return new AppError(
        ErrorType.NETWORK_ERROR,
        'Network error occurred',
        0
      );
    }

    const status = error.status;
    const body = error.error;

    switch (status) {
      case 401:
        return new AppError(
          ErrorType.UNAUTHORIZED,
          body?.message || 'Authentication required',
          status
        );
      case 403:
        return new AppError(
          ErrorType.FORBIDDEN,
          body?.message || 'Access denied',
          status
        );
      case 404:
        return new AppError(
          ErrorType.NOT_FOUND,
          body?.message || 'Resource not found',
          status
        );
      case 400:
        return new AppError(
          ErrorType.VALIDATION,
          body?.message || 'Validation failed',
          status,
          body?.errors
        );
      case 500:
      case 502:
      case 503:
      case 504:
        return new AppError(
          ErrorType.SERVER_ERROR,
          body?.message || 'Server error occurred',
          status
        );
      default:
        return new AppError(
          ErrorType.UNKNOWN,
          body?.message || 'An unexpected error occurred',
          status
        );
    }
  }

  isUnauthorized(): boolean {
    return this.type === ErrorType.UNAUTHORIZED;
  }

  isForbidden(): boolean {
    return this.type === ErrorType.FORBIDDEN;
  }

  isValidationError(): boolean {
    return this.type === ErrorType.VALIDATION;
  }

  isServerError(): boolean {
    return this.type === ErrorType.SERVER_ERROR;
  }

  isNetworkError(): boolean {
    return this.type === ErrorType.NETWORK_ERROR;
  }
}
