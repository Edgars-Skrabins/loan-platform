import { TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { LoanDetailComponent } from './loan-detail.component';
import { LoanStatusPipe } from '../../pipes/loan-status.pipe';
import { LoanService } from '../../../../core/services/loan.service';
import { AuthService } from '../../../../core/services/auth.service';
import { LoanApplication, LoanStatus } from '../../../../core/models/loan.model';
import { AuthUser, Role } from '../../../../core/models/auth.model';

describe('LoanDetailComponent', () => {
  let loanService: jasmine.SpyObj<LoanService>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;
  let activatedRoute: { snapshot: { paramMap: ReturnType<typeof convertToParamMap> } };

  const loan: LoanApplication = {
    id: 5,
    amount: 5000,
    termMonths: 24,
    status: LoanStatus.PENDING,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };

  beforeEach(async () => {
    loanService = jasmine.createSpyObj('LoanService', ['getLoanApplication', 'updateLoanApplicationStatus']);
    authService = jasmine.createSpyObj('AuthService', [], { currentUserValue: null as AuthUser | null });
    router = jasmine.createSpyObj('Router', ['navigate']);
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    activatedRoute = { snapshot: { paramMap: convertToParamMap({ id: '5' }) } };

    loanService.getLoanApplication.and.returnValue(of(loan));

    await TestBed.configureTestingModule({
      declarations: [LoanDetailComponent, LoanStatusPipe],
      imports: [CommonModule, ReactiveFormsModule],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: LoanService, useValue: loanService },
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(LoanDetailComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('loads the loan for the id in the route and seeds the status form', () => {
    const fixture = createComponent();

    expect(loanService.getLoanApplication).toHaveBeenCalledWith(5);
    expect(fixture.componentInstance.loan).toEqual(loan);
    expect(fixture.componentInstance.statusForm.value.newStatus).toBe(LoanStatus.PENDING);
    expect(fixture.componentInstance.loading).toBe(false);
  });

  it('navigates back to the list and shows an error when loading fails', () => {
    loanService.getLoanApplication.and.returnValue(throwError(() => ({ status: 404 })));

    const fixture = createComponent();

    expect(fixture.componentInstance.loading).toBe(false);
    expect(snackBar.open).toHaveBeenCalledWith('Failed to load loan details', 'Close', { duration: 5000 });
    expect(router.navigate).toHaveBeenCalledWith(['/loans']);
  });

  it('does not update when the form is invalid or the loan has not loaded', () => {
    const fixture = createComponent();
    fixture.componentInstance.statusForm.setValue({ newStatus: '' });

    fixture.componentInstance.updateStatus();

    expect(loanService.updateLoanApplicationStatus).not.toHaveBeenCalled();
  });

  it('updates the status and reloads the loan on success', () => {
    loanService.updateLoanApplicationStatus.and.returnValue(of({ id: 5, newStatus: LoanStatus.APPROVED }));
    const fixture = createComponent();
    fixture.componentInstance.statusForm.setValue({ newStatus: LoanStatus.APPROVED });

    fixture.componentInstance.updateStatus();

    expect(loanService.updateLoanApplicationStatus).toHaveBeenCalledWith(5, { newStatus: LoanStatus.APPROVED });
    expect(snackBar.open).toHaveBeenCalledWith('Loan status updated successfully', 'Close', { duration: 3000 });
    expect(fixture.componentInstance.updating).toBe(false);
    expect(loanService.getLoanApplication).toHaveBeenCalledTimes(2);
  });

  it('shows the server error message and stops updating when the update fails', () => {
    loanService.updateLoanApplicationStatus.and.returnValue(throwError(() => ({ error: { message: 'Cannot move from PENDING to PENDING' } })));
    const fixture = createComponent();
    fixture.componentInstance.statusForm.setValue({ newStatus: LoanStatus.APPROVED });

    fixture.componentInstance.updateStatus();

    expect(fixture.componentInstance.updating).toBe(false);
    expect(snackBar.open).toHaveBeenCalledWith('Cannot move from PENDING to PENDING', 'Close', { duration: 5000 });
  });

  it('allows ADMIN and LOAN_OFFICER to update status, but not CUSTOMER', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance;

    Object.defineProperty(authService, 'currentUserValue', { value: { role: Role.ADMIN } as AuthUser });
    component.currentUserRole = Role.ADMIN;
    expect(component.canUpdateStatus()).toBe(true);

    component.currentUserRole = Role.LOAN_OFFICER;
    expect(component.canUpdateStatus()).toBe(true);

    component.currentUserRole = Role.CUSTOMER;
    expect(component.canUpdateStatus()).toBe(false);
  });

  it('goBack navigates to the loan list', () => {
    const fixture = createComponent();

    fixture.componentInstance.goBack();

    expect(router.navigate).toHaveBeenCalledWith(['/loans']);
  });

  it('maps each status to its chip color', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance;

    expect(component.getStatusColor(LoanStatus.PENDING)).toBe('warn');
    expect(component.getStatusColor(LoanStatus.IN_REVIEW)).toBe('accent');
    expect(component.getStatusColor(LoanStatus.APPROVED)).toBe('primary');
    expect(component.getStatusColor(LoanStatus.REJECTED)).toBe('warn');
  });
});
