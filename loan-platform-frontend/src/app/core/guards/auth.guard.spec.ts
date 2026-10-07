import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { AuthUser, Role } from '../models/auth.model';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  const loggedInUser: AuthUser = { id: 1, email: 'ada@example.com', token: 't', role: Role.CUSTOMER };

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', [], {
      currentUserValue: null,
      isAuthenticated: false
    });
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    guard = TestBed.inject(AuthGuard);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  describe('canActivate', () => {
    it('should allow access when a user is loaded and authenticated', () => {
      Object.defineProperty(authService, 'currentUserValue', { value: loggedInUser });
      Object.defineProperty(authService, 'isAuthenticated', { value: true });

      const result = guard.canActivate(
        { data: {} } as any,
        { url: '/loans' } as any
      );

      expect(result).toBe(true);
    });

    it('should deny access when there is no current user, even if isAuthenticated is true', () => {
      Object.defineProperty(authService, 'currentUserValue', { value: null });
      Object.defineProperty(authService, 'isAuthenticated', { value: true });

      const result = guard.canActivate(
        { data: {} } as any,
        { url: '/loans' } as any
      );

      expect(result).toBe(false);
    });

    it('should deny access when a user is loaded but isAuthenticated is false', () => {
      Object.defineProperty(authService, 'currentUserValue', { value: loggedInUser });
      Object.defineProperty(authService, 'isAuthenticated', { value: false });

      const result = guard.canActivate(
        { data: {} } as any,
        { url: '/loans' } as any
      );

      expect(result).toBe(false);
    });

    it('should redirect to login with the attempted url as returnUrl when access is denied', () => {
      Object.defineProperty(authService, 'currentUserValue', { value: null });
      Object.defineProperty(authService, 'isAuthenticated', { value: false });

      guard.canActivate(
        { data: {} } as any,
        { url: '/loans/42' } as any
      );

      expect(router.navigate).toHaveBeenCalledWith(['/auth/login'], { queryParams: { returnUrl: '/loans/42' } });
    });

    it('should not navigate to login when access is allowed', () => {
      Object.defineProperty(authService, 'currentUserValue', { value: loggedInUser });
      Object.defineProperty(authService, 'isAuthenticated', { value: true });

      guard.canActivate(
        { data: {} } as any,
        { url: '/loans' } as any
      );

      expect(router.navigate).not.toHaveBeenCalled();
    });
  });
});
