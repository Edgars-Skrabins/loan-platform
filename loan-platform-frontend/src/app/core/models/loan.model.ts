export enum LoanStatus {
  PENDING = 'PENDING',
  IN_REVIEW = 'IN_REVIEW',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

export enum LoanDecisionOutcome {
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

export interface CreateLoanApplicationRequest {
  amount: number;
  termMonths: number;
}

export interface CreateLoanApplicationResponse {
  id: number;
  status: LoanStatus;
}

export interface LoanApplication {
  id: number;
  amount: number;
  termMonths: number;
  interestRate?: number;
  status: LoanStatus;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateLoanApplicationStatusRequest {
  id: number;
  newStatus: LoanStatus;
}

export interface UpdateLoanApplicationStatusResponse {
  id: number;
  newStatus: LoanStatus;
}

export interface DeleteLoanApplicationRequest {
  id: number;
}

export interface LoanDecision {
  id: number;
  loanApplicationId: number;
  officerId: number;
  decision: LoanDecisionOutcome;
  comment?: string;
  createdAt: string;
}
