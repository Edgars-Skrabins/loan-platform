import { TestBed } from '@angular/core/testing';
import { ErrorService } from './error.service';
import { AppError, ErrorType } from '../models/error.model';

describe('ErrorService', () => {
  let service: ErrorService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ErrorService]
    });

    service = TestBed.inject(ErrorService);
  });

  describe('setError', () => {
    it('should set error and emit it', (done) => {
      const error = new AppError(ErrorType.VALIDATION, 'Invalid input', 400);

      service.error$.subscribe((currentError) => {
        if (currentError) {
          expect(currentError).toEqual(error);
          done();
        }
      });

      service.setError(error);
    });
  });

  describe('clearError', () => {
    it('should clear error', (done) => {
      const error = new AppError(ErrorType.VALIDATION, 'Invalid input', 400);
      service.setError(error);

      service.clearError();

      service.error$.subscribe((currentError) => {
        expect(currentError).toBeNull();
        done();
      });
    });
  });

  describe('getCurrentError', () => {
    it('should return current error', () => {
      const error = new AppError(ErrorType.SERVER_ERROR, 'Server error', 500);
      service.setError(error);

      const currentError = service.getCurrentError();
      expect(currentError).toEqual(error);
    });

    it('should return null when no error is set', () => {
      service.clearError();
      const currentError = service.getCurrentError();
      expect(currentError).toBeNull();
    });
  });

  describe('getErrorMessage', () => {
    it('should return message for unauthorized error', () => {
      const error = new AppError(ErrorType.UNAUTHORIZED, 'Auth required', 401);
      const message = service.getErrorMessage(error);
      expect(message).toContain('session has expired');
    });

    it('should return message for validation error', () => {
      const error = new AppError(ErrorType.VALIDATION, 'Invalid data', 400);
      const message = service.getErrorMessage(error);
      expect(message).toContain('check your input');
    });

    it('should return message for server error', () => {
      const error = new AppError(ErrorType.SERVER_ERROR, 'Server failed', 500);
      const message = service.getErrorMessage(error);
      expect(message).toContain('server error');
    });

    it('should return custom error message if available', () => {
      const customMessage = 'Custom error occurred';
      const error = new AppError(ErrorType.UNKNOWN, customMessage, 0);
      const message = service.getErrorMessage(error);
      expect(message).toBe(customMessage);
    });
  });

  describe('handleError', () => {
    it('should convert HTTP error and set it', () => {
      const httpError = {
        status: 401,
        error: { message: 'Unauthorized access' }
      };

      const error = service.handleError(httpError);

      expect(error.type).toBe(ErrorType.UNAUTHORIZED);
      expect(error.statusCode).toBe(401);
      expect(service.getCurrentError()).toEqual(error);
    });

    it('should handle 404 errors', () => {
      const httpError = {
        status: 404,
        error: { message: 'Not found' }
      };

      const error = service.handleError(httpError);

      expect(error.type).toBe(ErrorType.NOT_FOUND);
      expect(error.statusCode).toBe(404);
    });

    it('should handle 500 server errors', () => {
      const httpError = {
        status: 500,
        error: { message: 'Internal server error' }
      };

      const error = service.handleError(httpError);

      expect(error.type).toBe(ErrorType.SERVER_ERROR);
      expect(error.statusCode).toBe(500);
    });

    it('should handle network errors', () => {
      const httpError = {
        status: 0,
        error: {}
      };

      const error = service.handleError(httpError);

      expect(error.type).toBe(ErrorType.NETWORK_ERROR);
      expect(error.statusCode).toBe(0);
    });
  });

  describe('AppError helper methods', () => {
    it('isUnauthorized should return true for unauthorized errors', () => {
      const error = new AppError(ErrorType.UNAUTHORIZED, 'Unauthorized', 401);
      expect(error.isUnauthorized()).toBe(true);
    });

    it('isForbidden should return true for forbidden errors', () => {
      const error = new AppError(ErrorType.FORBIDDEN, 'Forbidden', 403);
      expect(error.isForbidden()).toBe(true);
    });

    it('isValidationError should return true for validation errors', () => {
      const error = new AppError(ErrorType.VALIDATION, 'Validation failed', 400);
      expect(error.isValidationError()).toBe(true);
    });

    it('isServerError should return true for server errors', () => {
      const error = new AppError(ErrorType.SERVER_ERROR, 'Server error', 500);
      expect(error.isServerError()).toBe(true);
    });

    it('isNetworkError should return true for network errors', () => {
      const error = new AppError(ErrorType.NETWORK_ERROR, 'Network error', 0);
      expect(error.isNetworkError()).toBe(true);
    });
  });
});
