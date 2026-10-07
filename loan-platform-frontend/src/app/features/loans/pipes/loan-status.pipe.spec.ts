import { LoanStatusPipe } from './loan-status.pipe';
import { LoanStatus } from '../../../core/models/loan.model';

describe('LoanStatusPipe', () => {
  let pipe: LoanStatusPipe;

  beforeEach(() => {
    pipe = new LoanStatusPipe();
  });

  it('creates an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('maps PENDING to "Pending Review"', () => {
    expect(pipe.transform(LoanStatus.PENDING)).toBe('Pending Review');
  });

  it('maps IN_REVIEW to "Under Review"', () => {
    expect(pipe.transform(LoanStatus.IN_REVIEW)).toBe('Under Review');
  });

  it('maps APPROVED to "Approved"', () => {
    expect(pipe.transform(LoanStatus.APPROVED)).toBe('Approved');
  });

  it('maps REJECTED to "Rejected"', () => {
    expect(pipe.transform(LoanStatus.REJECTED)).toBe('Rejected');
  });
});
