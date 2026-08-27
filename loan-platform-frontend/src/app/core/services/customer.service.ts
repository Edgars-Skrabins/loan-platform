import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CustomerProfile, UpdateCustomerProfileRequest, UpdateCustomerProfileResponse } from '../models/customer.model';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private apiUrl = `${environment.apiBaseUrl}/customers`;

  constructor(private http: HttpClient) { }

  getProfile(): Observable<CustomerProfile> {
    return this.http.get<CustomerProfile>(`${this.apiUrl}/profile`);
  }

  updateProfile(request: UpdateCustomerProfileRequest): Observable<UpdateCustomerProfileResponse> {
    return this.http.put<UpdateCustomerProfileResponse>(`${this.apiUrl}/profile`, request);
  }
}
