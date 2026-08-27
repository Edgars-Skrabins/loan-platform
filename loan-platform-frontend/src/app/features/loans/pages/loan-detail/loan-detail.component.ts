import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { LoanService } from '../../../../core/services/loan.service';
import { AuthService } from '../../../../core/services/auth.service';
import { LoanApplication, LoanStatus, UpdateLoanApplicationStatusRequest } from '../../../../core/models/loan.model';
import { Role } from '../../../../core/models/auth.model';

@Component({
  selector: 'app-loan-detail',
  templateUrl: './loan-detail.component.html',
  styleUrls: ['./loan-detail.component.scss']
})
export class LoanDetailComponent implements OnInit, OnDestroy {
  loan: LoanApplication | null = null;
  statusForm!: FormGroup;
  loading = true;
  updating = false;
  currentUserRole: Role | null = null;
  LoanStatus = LoanStatus;
  loanStatusOptions = [
    { value: LoanStatus.PENDING, label: 'Pending' },
    { value: LoanStatus.IN_REVIEW, label: 'In Review' },
    { value: LoanStatus.APPROVED, label: 'Approved' },
    { value: LoanStatus.REJECTED, label: 'Rejected' }
  ];

  private destroy$ = new Subject<void>();
  private loanId: number = 0;

  constructor(
    private loanService: LoanService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.currentUserRole = this.authService.currentUserValue?.role || null;
    this.loanId = parseInt(this.route.snapshot.paramMap.get('id') || '0', 10);
    this.initializeForm();
    this.loadLoan();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeForm(): void {
    this.statusForm = this.formBuilder.group({
      newStatus: ['', Validators.required]
    });
  }

  private loadLoan(): void {
    this.loading = true;
    this.loanService.getLoanApplication(this.loanId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (loan) => {
          this.loan = loan;
          this.statusForm.patchValue({ newStatus: loan.status });
          this.loading = false;
        },
        error: (error) => {
          this.loading = false;
          this.snackBar.open('Failed to load loan details', 'Close', { duration: 5000 });
          this.router.navigate(['/loans']);
        }
      });
  }

  updateStatus(): void {
    if (this.statusForm.invalid || !this.loan) {
      return;
    }

    this.updating = true;
    const request: UpdateLoanApplicationStatusRequest = {
      id: this.loan.id,
      newStatus: this.statusForm.value.newStatus
    };

    this.loanService.updateLoanApplicationStatus(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.snackBar.open('Loan status updated successfully', 'Close', { duration: 3000 });
          this.updating = false;
          this.loadLoan();
        },
        error: (error) => {
          this.updating = false;
          const errorMessage = error?.error?.message || 'Failed to update loan status';
          this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        }
      });
  }

  canUpdateStatus(): boolean {
    return this.currentUserRole === Role.ADMIN || this.currentUserRole === 'LOAN_OFFICER' as any;
  }

  goBack(): void {
    this.router.navigate(['/loans']);
  }

  getStatusColor(status: LoanStatus): string {
    const colorMap: Record<LoanStatus, string> = {
      [LoanStatus.PENDING]: 'warn',
      [LoanStatus.IN_REVIEW]: 'accent',
      [LoanStatus.APPROVED]: 'primary',
      [LoanStatus.REJECTED]: 'warn'
    };
    return colorMap[status];
  }
}
