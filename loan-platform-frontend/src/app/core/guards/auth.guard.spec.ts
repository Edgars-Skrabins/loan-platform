import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/auth.model';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isAuthenticated']);
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
    it('should allow access when user is authenticated', () => {
      authService.isAuthenticated.and.returnValue(true);

      const result = guard.canActivate(
        { data: {} } as any,
        { url: '/loans' } as any
      );

      expect(result).toBe(true);
    });

    it('should deny access when user is not authenticated', () => {
      authService.isAuthenticated.and.returnValue(false);

      const result = guard.canActivate(
        { data: {} } as any,
        { url: '/loans' } as any
      );

      expect(result).toBe(false);
    });

    it('should navigate to login when access is denied', () => {
      authService.isAuthenticated.and.returnValue(false);

      guard.canActivate(
        { data: {} } as any,
        { url: '/loans' } as any
      );

      expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
    });

    it('should not navigate to login when access is allowed', () => {
      authService.isAuthenticated.and.returnValue(true);

      guard.canActivate(
        { data: {} } as any,
        { url: '/loans' } as any
      );

      expect(router.navigate).not.toHaveBeenCalled();
    });
  });
});
