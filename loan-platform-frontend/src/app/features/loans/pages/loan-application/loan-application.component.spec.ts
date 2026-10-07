import { TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { LoanApplicationComponent } from './loan-application.component';
import { LoanService } from '../../../../core/services/loan.service';
import { LoanStatus } from '../../../../core/models/loan.model';

describe('LoanApplicationComponent', () => {
  let loanService: jasmine.SpyObj<LoanService>;
  let router: jasmine.SpyObj<Router>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    loanService = jasmine.createSpyObj('LoanService', ['createLoanApplication']);
    router = jasmine.createSpyObj('Router', ['navigate']);
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      declarations: [LoanApplicationComponent],
      imports: [CommonModule, ReactiveFormsModule],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: LoanService, useValue: loanService },
        { provide: Router, useValue: router },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();
  });

  function createComponent() {
    const fixture = TestBed.createComponent(LoanApplicationComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('should create', () => {
    const fixture = createComponent();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('does not submit an invalid form', () => {
    const fixture = createComponent();

    fixture.componentInstance.onSubmit();

    expect(fixture.componentInstance.submitted).toBe(true);
    expect(loanService.createLoanApplication).not.toHaveBeenCalled();
  });

  it('rejects an amount below 100 and a term above 480', () => {
    const fixture = createComponent();
    fixture.componentInstance.applicationForm.setValue({ amount: 50, termMonths: 481 });

    expect(fixture.componentInstance.applicationForm.invalid).toBe(true);
    expect(fixture.componentInstance.f['amount'].hasError('min')).toBe(true);
    expect(fixture.componentInstance.f['termMonths'].hasError('max')).toBe(true);
  });

  it('submits and navigates to /loans on success', () => {
    loanService.createLoanApplication.and.returnValue(of({ id: 1, status: LoanStatus.PENDING }));
    const fixture = createComponent();
    fixture.componentInstance.applicationForm.setValue({ amount: 5000, termMonths: 24 });

    fixture.componentInstance.onSubmit();

    expect(loanService.createLoanApplication).toHaveBeenCalledWith({ amount: 5000, termMonths: 24 });
    expect(snackBar.open).toHaveBeenCalledWith('Loan application submitted successfully!', 'Close', { duration: 3000 });
    expect(router.navigate).toHaveBeenCalledWith(['/loans']);
  });

  it('shows the server error message and stops loading when submission fails', () => {
    loanService.createLoanApplication.and.returnValue(throwError(() => ({ error: { message: 'Amount too high' } })));
    const fixture = createComponent();
    fixture.componentInstance.applicationForm.setValue({ amount: 5000, termMonths: 24 });

    fixture.componentInstance.onSubmit();

    expect(fixture.componentInstance.loading).toBe(false);
    expect(snackBar.open).toHaveBeenCalledWith('Amount too high', 'Close', { duration: 5000 });
  });

  it('falls back to a generic message when the error has none', () => {
    loanService.createLoanApplication.and.returnValue(throwError(() => ({})));
    const fixture = createComponent();
    fixture.componentInstance.applicationForm.setValue({ amount: 5000, termMonths: 24 });

    fixture.componentInstance.onSubmit();

    expect(snackBar.open).toHaveBeenCalledWith('Failed to submit loan application', 'Close', { duration: 5000 });
  });

  it('cancel navigates to /dashboard', () => {
    const fixture = createComponent();

    fixture.componentInstance.cancel();

    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('goBack navigates to /dashboard', () => {
    const fixture = createComponent();

    fixture.componentInstance.goBack();

    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });
});
