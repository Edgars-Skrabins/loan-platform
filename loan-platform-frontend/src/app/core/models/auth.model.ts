export enum Role {
  CUSTOMER = 'CUSTOMER',
  LOAN_OFFICER = 'LOAN_OFFICER',
  ADMIN = 'ADMIN'
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  id: number;
  token: string;
  email: string;
  role: Role;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface RegisterResponse {
  id: number;
  email: string;
  role: Role;
}

export interface AuthUser {
  id: number;
  email: string;
  token: string;
  role: Role;
}
