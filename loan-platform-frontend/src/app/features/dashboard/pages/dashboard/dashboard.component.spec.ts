import { TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { AuthService } from '../../../../core/services/auth.service';
import { AuthUser, Role } from '../../../../core/models/auth.model';

describe('DashboardComponent', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let currentUserSubject: BehaviorSubject<AuthUser | null>;

  beforeEach(async () => {
    currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
    authService = jasmine.createSpyObj('AuthService', ['logout'], {
      currentUser$: currentUserSubject.asObservable()
    });
    router = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      imports: [CommonModule],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('starts with no current user when none is emitted yet', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance.currentUser).toBeNull();
  });

  it('updates currentUser when the auth service emits one', () => {
    const fixture = createComponent();
    const user: AuthUser = { id: 1, email: 'ada@example.com', token: 't', role: Role.LOAN_OFFICER };

    currentUserSubject.next(user);

    expect(fixture.componentInstance.currentUser).toEqual(user);
  });

  it('logs out and navigates to login', () => {
    const fixture = createComponent();

    fixture.componentInstance.logout();

    expect(authService.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/login']);
  });

  it('stops reacting to currentUser$ after destroy', () => {
    const fixture = createComponent();
    fixture.destroy();

    currentUserSubject.next({ id: 2, email: 'other@example.com', token: 't2', role: Role.ADMIN });

    expect(fixture.componentInstance.currentUser).toBeNull();
  });
});
