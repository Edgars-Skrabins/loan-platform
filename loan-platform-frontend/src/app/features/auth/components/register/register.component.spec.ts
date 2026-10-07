import { TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../../../core/services/auth.service';
import { AuthUser, Role } from '../../../../core/models/auth.model';

describe('RegisterComponent', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj('AuthService', ['register'], { currentUserValue: null });
    router = jasmine.createSpyObj('Router', ['navigate']);
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      imports: [CommonModule, ReactiveFormsModule, MatCheckboxModule, NoopAnimationsModule],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(RegisterComponent);
    fixture.detectChanges();
    return fixture;
  }

  function fillValidForm(fixture: ReturnType<typeof createComponent>): void {
    fixture.componentInstance.registerForm.setValue({
      email: 'ada@example.com',
      password: 'password123',
      confirmPassword: 'password123',
      agreeToTerms: true
    });
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

  it('does not submit an empty form', () => {
    const fixture = createComponent();

    fixture.componentInstance.onSubmit();

    expect(fixture.componentInstance.submitted).toBe(true);
    expect(authService.register).not.toHaveBeenCalled();
  });

  it('flags mismatched passwords on the confirmPassword control', () => {
    const fixture = createComponent();
    fixture.componentInstance.registerForm.setValue({
      email: 'ada@example.com',
      password: 'password123',
      confirmPassword: 'different123',
      agreeToTerms: true
    });

    expect(fixture.componentInstance.registerForm.hasError('passwordMismatch')).toBe(true);
    expect(fixture.componentInstance.f['confirmPassword'].hasError('passwordMismatch')).toBe(true);
  });

  it('requires agreeToTerms to be checked', () => {
    const fixture = createComponent();
    fixture.componentInstance.registerForm.setValue({
      email: 'ada@example.com',
      password: 'password123',
      confirmPassword: 'password123',
      agreeToTerms: false
    });

    expect(fixture.componentInstance.registerForm.invalid).toBe(true);
  });

  it('registers and navigates to login on success', () => {
    authService.register.and.returnValue(of({ id: 2, email: 'ada@example.com', role: Role.CUSTOMER }));
    const fixture = createComponent();
    fillValidForm(fixture);

    fixture.componentInstance.onSubmit();

    expect(authService.register).toHaveBeenCalledWith({ email: 'ada@example.com', password: 'password123' });
    expect(snackBar.open).toHaveBeenCalledWith('Registration successful! Please log in.', 'Close', { duration: 3000 });
    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
  });

  it('shows the server error message and stops loading when registration fails', () => {
    authService.register.and.returnValue(throwError(() => ({ error: { message: 'Email already in use' } })));
    const fixture = createComponent();
    fillValidForm(fixture);

    fixture.componentInstance.onSubmit();

    expect(fixture.componentInstance.loading).toBe(false);
    expect(snackBar.open).toHaveBeenCalledWith('Email already in use', 'Close', { duration: 5000 });
  });

  it('falls back to a generic message when the error has none', () => {
    authService.register.and.returnValue(throwError(() => ({})));
    const fixture = createComponent();
    fillValidForm(fixture);

    fixture.componentInstance.onSubmit();

    expect(snackBar.open).toHaveBeenCalledWith('Registration failed. Please try again.', 'Close', { duration: 5000 });
  });

  it('toggles password and confirm-password visibility independently', () => {
    const fixture = createComponent();

    expect(fixture.componentInstance.hidePassword).toBe(true);
    expect(fixture.componentInstance.hideConfirmPassword).toBe(true);

    fixture.componentInstance.togglePasswordVisibility();
    expect(fixture.componentInstance.hidePassword).toBe(false);
    expect(fixture.componentInstance.hideConfirmPassword).toBe(true);

    fixture.componentInstance.toggleConfirmPasswordVisibility();
    expect(fixture.componentInstance.hideConfirmPassword).toBe(false);
  });

  it('navigates to the login page', () => {
    const fixture = createComponent();

    fixture.componentInstance.goToLogin();

    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
