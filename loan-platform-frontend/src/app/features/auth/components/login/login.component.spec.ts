import { TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../../core/services/auth.service';
import { AuthUser, Role } from '../../../../core/models/auth.model';

describe('LoginComponent', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;
  let activatedRoute: { snapshot: { queryParams: Record<string, string> } };

  beforeEach(async () => {
    authService = jasmine.createSpyObj('AuthService', ['login'], { currentUserValue: null });
    router = jasmine.createSpyObj('Router', ['navigate']);
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    activatedRoute = { snapshot: { queryParams: {} } };

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [CommonModule, ReactiveFormsModule],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('redirects to the dashboard immediately when already logged in', () => {
    const loggedInUser: AuthUser = { id: 1, email: 'ada@example.com', token: 't', role: Role.CUSTOMER };
    Object.defineProperty(authService, 'currentUserValue', { value: loggedInUser });

    createComponent();

    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('defaults returnUrl to /dashboard when no query param is present', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance.returnUrl).toBe('/dashboard');
  });

  it('uses the returnUrl query param when present', () => {
    activatedRoute.snapshot.queryParams = { returnUrl: '/loans' };
    const fixture = createComponent();
    expect(fixture.componentInstance.returnUrl).toBe('/loans');
  });

  it('does not submit an invalid form', () => {
    const fixture = createComponent();

    fixture.componentInstance.onSubmit();

    expect(fixture.componentInstance.submitted).toBe(true);
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('logs in and navigates to returnUrl on success', () => {
    authService.login.and.returnValue(of({ id: 1, token: 't', email: 'ada@example.com', role: Role.CUSTOMER }));
    const fixture = createComponent();
    fixture.componentInstance.returnUrl = '/loans';
    fixture.componentInstance.loginForm.setValue({ email: 'ada@example.com', password: 'password123' });

    fixture.componentInstance.onSubmit();

    expect(authService.login).toHaveBeenCalledWith({ email: 'ada@example.com', password: 'password123' });
    expect(snackBar.open).toHaveBeenCalledWith('Login successful!', 'Close', { duration: 3000 });
    expect(router.navigate).toHaveBeenCalledWith(['/loans']);
  });

  it('shows the server error message and stops loading when login fails', () => {
    authService.login.and.returnValue(throwError(() => ({ error: { message: 'Invalid credentials' } })));
    const fixture = createComponent();
    fixture.componentInstance.loginForm.setValue({ email: 'ada@example.com', password: 'password123' });

    fixture.componentInstance.onSubmit();

    expect(fixture.componentInstance.loading).toBe(false);
    expect(snackBar.open).toHaveBeenCalledWith('Invalid credentials', 'Close', { duration: 5000 });
  });

  it('falls back to a generic message when the error has none', () => {
    authService.login.and.returnValue(throwError(() => ({})));
    const fixture = createComponent();
    fixture.componentInstance.loginForm.setValue({ email: 'ada@example.com', password: 'password123' });

    fixture.componentInstance.onSubmit();

    expect(snackBar.open).toHaveBeenCalledWith('Login failed. Please check your credentials.', 'Close', { duration: 5000 });
  });

  it('toggles password visibility', () => {
    const fixture = createComponent();

    expect(fixture.componentInstance.hidePassword).toBe(true);
    fixture.componentInstance.togglePasswordVisibility();
    expect(fixture.componentInstance.hidePassword).toBe(false);
  });

  it('navigates to the register page', () => {
    const fixture = createComponent();

    fixture.componentInstance.goToRegister();

    expect(router.navigate).toHaveBeenCalledWith(['/auth/register']);
  });
});
