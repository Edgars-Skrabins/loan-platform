export interface CustomerProfile {
  id: number;
  userId: number;
  monthlyIncome?: number;
  employmentStatus?: string;
  creditScore?: number;
}

export interface UpdateCustomerProfileRequest {
  monthlyIncome?: number;
  employmentStatus?: string;
  creditScore?: number;
}

export interface UpdateCustomerProfileResponse {
  id: number;
  userId: number;
  monthlyIncome?: number;
  employmentStatus?: string;
  creditScore?: number;
}
