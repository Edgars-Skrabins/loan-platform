import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { AppError, ErrorType } from '../models/error.model';

@Injectable({
  providedIn: 'root'
})
export class ErrorService {
  private errorSubject = new BehaviorSubject<AppError | null>(null);
  public error$ = this.errorSubject.asObservable();

  constructor() {}

  setError(error: AppError): void {
    this.errorSubject.next(error);
  }

  clearError(): void {
    this.errorSubject.next(null);
  }

  getCurrentError(): AppError | null {
    return this.errorSubject.value;
  }

  getErrorMessage(error: AppError): string {
    const messages: Record<ErrorType, string> = {
      [ErrorType.UNAUTHORIZED]: 'Your session has expired. Please log in again.',
      [ErrorType.FORBIDDEN]: 'You do not have permission to perform this action.',
      [ErrorType.NOT_FOUND]: 'The requested resource was not found.',
      [ErrorType.VALIDATION]: 'Please check your input and try again.',
      [ErrorType.SERVER_ERROR]: 'A server error occurred. Please try again later.',
      [ErrorType.NETWORK_ERROR]: 'Network connection error. Please check your internet.',
      [ErrorType.UNKNOWN]: 'An unexpected error occurred. Please try again.'
    };

    return error.message || messages[error.type] || messages[ErrorType.UNKNOWN];
  }

  handleError(error: any): AppError {
    const appError = AppError.fromHttpError(error);
    this.setError(appError);
    return appError;
  }
}
