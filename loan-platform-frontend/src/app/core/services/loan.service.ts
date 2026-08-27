import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateLoanApplicationRequest,
  CreateLoanApplicationResponse,
  LoanApplication,
  UpdateLoanApplicationStatusRequest,
  UpdateLoanApplicationStatusResponse,
  DeleteLoanApplicationRequest
} from '../models/loan.model';

@Injectable({
  providedIn: 'root'
})
export class LoanService {
  private apiUrl = `${environment.apiBaseUrl}/loans`;

  constructor(private http: HttpClient) { }

  createLoanApplication(request: CreateLoanApplicationRequest): Observable<CreateLoanApplicationResponse> {
    return this.http.post<CreateLoanApplicationResponse>(`${this.apiUrl}/loan-application`, request);
  }

  getLoanApplications(): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(`${this.apiUrl}/loan-applications`);
  }

  getLoanApplication(id: number): Observable<LoanApplication> {
    return this.http.get<LoanApplication>(`${this.apiUrl}/loan-application/${id}`);
  }

  updateLoanApplicationStatus(request: UpdateLoanApplicationStatusRequest): Observable<UpdateLoanApplicationStatusResponse> {
    return this.http.put<UpdateLoanApplicationStatusResponse>(`${this.apiUrl}/loan-application/${request.id}`, request);
  }

  deleteLoanApplication(id: number): Observable<void> {
    const request: DeleteLoanApplicationRequest = { id };
    return this.http.delete<void>(`${this.apiUrl}/loan-application/${id}`, { body: request });
  }
}
