import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { LoanService } from '../../../../core/services/loan.service';
import { AuthService } from '../../../../core/services/auth.service';
import { LoanApplication, LoanStatus } from '../../../../core/models/loan.model';
import { Role } from '../../../../core/models/auth.model';

@Component({
  selector: 'app-loan-list',
  templateUrl: './loan-list.component.html',
  styleUrls: ['./loan-list.component.scss']
})
export class LoanListComponent implements OnInit, OnDestroy {
  loans: LoanApplication[] = [];
  loading = false;
  currentUserRole: Role | null = null;

  displayedColumns: string[] = ['id', 'amount', 'termMonths', 'status', 'createdAt', 'actions'];
  LoanStatus = LoanStatus;

  private destroy$ = new Subject<void>();

  constructor(
    private loanService: LoanService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.currentUserRole = this.authService.currentUserValue?.role || null;
    this.loadLoans();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadLoans(): void {
    this.loading = true;
    this.loanService.getLoanApplications()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (loans) => {
          this.loans = loans;
          this.loading = false;
        },
        error: (error) => {
          this.loading = false;
          this.snackBar.open('Failed to load loans', 'Close', { duration: 5000 });
        }
      });
  }

  viewLoan(id: number): void {
    this.router.navigate(['/loans', id]);
  }

  deleteLoan(id: number): void {
    if (confirm('Are you sure you want to delete this loan application?')) {
      this.loanService.deleteLoanApplication(id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.snackBar.open('Loan application deleted', 'Close', { duration: 3000 });
            this.loadLoans();
          },
          error: (error) => {
            this.snackBar.open('Failed to delete loan application', 'Close', { duration: 5000 });
          }
        });
    }
  }

  applyForLoan(): void {
    this.router.navigate(['/loans/apply']);
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
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

  canDelete(loan: LoanApplication): boolean {
    if (this.currentUserRole === Role.ADMIN || this.currentUserRole === 'LOAN_OFFICER' as any) {
      return loan.status === LoanStatus.PENDING;
    }
    return loan.status === LoanStatus.PENDING;
  }
}
