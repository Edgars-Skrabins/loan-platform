import { TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { LoanListComponent } from './loan-list.component';
import { LoanStatusPipe } from '../../pipes/loan-status.pipe';
import { LoanService } from '../../../../core/services/loan.service';
import { AuthService } from '../../../../core/services/auth.service';
import { LoanApplication, LoanStatus } from '../../../../core/models/loan.model';
import { AuthUser, Role } from '../../../../core/models/auth.model';

describe('LoanListComponent', () => {
  let loanService: jasmine.SpyObj<LoanService>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  const pendingLoan: LoanApplication = {
    id: 1,
    amount: 5000,
    termMonths: 24,
    status: LoanStatus.PENDING,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  const approvedLoan: LoanApplication = {
    ...pendingLoan,
    id: 2,
    status: LoanStatus.APPROVED
  };

  beforeEach(async () => {
    loanService = jasmine.createSpyObj('LoanService', ['getLoanApplications', 'deleteLoanApplication']);
    authService = jasmine.createSpyObj('AuthService', [], { currentUserValue: null as AuthUser | null });
    router = jasmine.createSpyObj('Router', ['navigate']);
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    loanService.getLoanApplications.and.returnValue(of([pendingLoan, approvedLoan]));

    await TestBed.configureTestingModule({
      declarations: [LoanListComponent, LoanStatusPipe],
      imports: [CommonModule],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: LoanService, useValue: loanService },
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(LoanListComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('loads the current user role and the loan list on init', () => {
    const loggedInUser: AuthUser = { id: 1, email: 'ada@example.com', token: 't', role: Role.LOAN_OFFICER };
    Object.defineProperty(authService, 'currentUserValue', { value: loggedInUser });

    const fixture = createComponent();

    expect(fixture.componentInstance.currentUserRole).toBe(Role.LOAN_OFFICER);
    expect(fixture.componentInstance.loans).toEqual([pendingLoan, approvedLoan]);
    expect(fixture.componentInstance.loading).toBe(false);
  });

  it('shows an error message when loading loans fails', () => {
    loanService.getLoanApplications.and.returnValue(throwError(() => ({ status: 500 })));

    const fixture = createComponent();

    expect(fixture.componentInstance.loading).toBe(false);
    expect(snackBar.open).toHaveBeenCalledWith('Failed to load loans', 'Close', { duration: 5000 });
  });

  it('viewLoan navigates to the loan detail page', () => {
    const fixture = createComponent();

    fixture.componentInstance.viewLoan(1);

    expect(router.navigate).toHaveBeenCalledWith(['/loans', 1]);
  });

  it('deletes a loan after confirming and reloads the list', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    loanService.deleteLoanApplication.and.returnValue(of(undefined));
    const fixture = createComponent();

    fixture.componentInstance.deleteLoan(1);

    expect(loanService.deleteLoanApplication).toHaveBeenCalledWith(1);
    expect(snackBar.open).toHaveBeenCalledWith('Loan application deleted', 'Close', { duration: 3000 });
  });

  it('does not delete a loan when the confirmation is cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    const fixture = createComponent();

    fixture.componentInstance.deleteLoan(1);

    expect(loanService.deleteLoanApplication).not.toHaveBeenCalled();
  });

  it('shows an error message when deletion fails', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    loanService.deleteLoanApplication.and.returnValue(throwError(() => ({ status: 409 })));
    const fixture = createComponent();

    fixture.componentInstance.deleteLoan(1);

    expect(snackBar.open).toHaveBeenCalledWith('Failed to delete loan application', 'Close', { duration: 5000 });
  });

  it('applyForLoan navigates to the apply page', () => {
    const fixture = createComponent();

    fixture.componentInstance.applyForLoan();

    expect(router.navigate).toHaveBeenCalledWith(['/loans/apply']);
  });

  it('goBack navigates to the dashboard', () => {
    const fixture = createComponent();

    fixture.componentInstance.goBack();

    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('maps each status to its chip color', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance;

    expect(component.getStatusColor(LoanStatus.PENDING)).toBe('warn');
    expect(component.getStatusColor(LoanStatus.IN_REVIEW)).toBe('accent');
    expect(component.getStatusColor(LoanStatus.APPROVED)).toBe('primary');
    expect(component.getStatusColor(LoanStatus.REJECTED)).toBe('warn');
  });

  it('only allows deleting a PENDING loan', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance;

    expect(component.canDelete(pendingLoan)).toBe(true);
    expect(component.canDelete(approvedLoan)).toBe(false);
  });
});
