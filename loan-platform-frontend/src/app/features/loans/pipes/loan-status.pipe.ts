import { Pipe, PipeTransform } from '@angular/core';
import { LoanStatus } from '../../../core/models/loan.model';

@Pipe({
  name: 'loanStatus'
})
export class LoanStatusPipe implements PipeTransform {
  transform(status: LoanStatus): string {
    const statusMap: Record<LoanStatus, string> = {
      [LoanStatus.PENDING]: 'Pending Review',
      [LoanStatus.IN_REVIEW]: 'Under Review',
      [LoanStatus.APPROVED]: 'Approved',
      [LoanStatus.REJECTED]: 'Rejected'
    };
    return statusMap[status] || status;
  }
}
