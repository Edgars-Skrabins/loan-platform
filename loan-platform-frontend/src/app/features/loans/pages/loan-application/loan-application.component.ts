import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { LoanService } from '../../../../core/services/loan.service';
import { CreateLoanApplicationRequest } from '../../../../core/models/loan.model';

@Component({
  selector: 'app-loan-application',
  templateUrl: './loan-application.component.html',
  styleUrls: ['./loan-application.component.scss']
})
export class LoanApplicationComponent implements OnInit, OnDestroy {
  applicationForm!: FormGroup;
  loading = false;
  submitted = false;

  private destroy$ = new Subject<void>();

  constructor(
    private formBuilder: FormBuilder,
    private loanService: LoanService,
    private router: Router,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.initializeForm();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeForm(): void {
    this.applicationForm = this.formBuilder.group({
      amount: ['', [Validators.required, Validators.min(100)]],
      termMonths: ['', [Validators.required, Validators.min(1), Validators.max(480)]]
    });
  }

  get f() {
    return this.applicationForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.applicationForm.invalid) {
      return;
    }

    this.loading = true;
    const request: CreateLoanApplicationRequest = {
      amount: this.f['amount'].value,
      termMonths: this.f['termMonths'].value
    };

    this.loanService.createLoanApplication(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.snackBar.open('Loan application submitted successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/loans']);
        },
        error: (error) => {
          this.loading = false;
          const errorMessage = error?.error?.message || 'Failed to submit loan application';
          this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/loans']);
  }
}
