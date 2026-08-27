import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LoanService } from './loan.service';
import { CreateLoanApplicationRequest, LoanApplication, LoanStatus } from '../models/loan.model';

describe('LoanService', () => {
  let service: LoanService;
  let httpMock: HttpTestingController;

  const mockLoanApplication: LoanApplication = {
    id: 1,
    customerId: 1,
    amount: 50000,
    termMonths: 60,
    interestRate: 5.5,
    status: LoanStatus.PENDING,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    customer: {
      id: 1,
      userId: 1,
      monthlyIncome: 5000,
      employmentStatus: 'EMPLOYED',
      creditScore: 750,
      user: {
        id: 1,
        email: 'test@example.com',
        role: 'CUSTOMER',
        createdAt: new Date().toISOString()
      }
    }
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LoanService]
    });

    service = TestBed.inject(LoanService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('createLoanApplication', () => {
    it('should send POST request to create loan', (done) => {
      const createRequest: CreateLoanApplicationRequest = {
        amount: 50000,
        termMonths: 60
      };

      service.createLoanApplication(createRequest).subscribe((response) => {
        expect(response).toEqual(mockLoanApplication);
        done();
      });

      const req = httpMock.expectOne('/api/loans/loan-application');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createRequest);
      req.flush(mockLoanApplication);
    });

    it('should include Authorization header', (done) => {
      const createRequest: CreateLoanApplicationRequest = {
        amount: 50000,
        termMonths: 60
      };

      service.createLoanApplication(createRequest).subscribe(() => {
        done();
      });

      const req = httpMock.expectOne('/api/loans/loan-application');
      expect(req.request.headers.has('Authorization')).toBeTruthy();
      req.flush(mockLoanApplication);
    });
  });

  describe('getLoanApplications', () => {
    it('should fetch all loan applications', (done) => {
      const mockLoans = [mockLoanApplication];

      service.getLoanApplications().subscribe((response) => {
        expect(response).toEqual(mockLoans);
        expect(response.length).toBe(1);
        done();
      });

      const req = httpMock.expectOne('/api/loans/loan-applications');
      expect(req.request.method).toBe('GET');
      req.flush(mockLoans);
    });

    it('should handle empty loan list', (done) => {
      service.getLoanApplications().subscribe((response) => {
        expect(response).toEqual([]);
        expect(response.length).toBe(0);
        done();
      });

      const req = httpMock.expectOne('/api/loans/loan-applications');
      req.flush([]);
    });
  });

  describe('getLoanApplication', () => {
    it('should fetch single loan application by id', (done) => {
      const loanId = 1;

      service.getLoanApplication(loanId).subscribe((response) => {
        expect(response).toEqual(mockLoanApplication);
        expect(response.id).toBe(1);
        done();
      });

      const req = httpMock.expectOne(`/api/loans/loan-application/${loanId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockLoanApplication);
    });
  });

  describe('updateLoanApplicationStatus', () => {
    it('should send PUT request to update loan status', (done) => {
      const loanId = 1;
      const newStatus = LoanStatus.APPROVED;
      const updatedLoan = { ...mockLoanApplication, status: newStatus };

      service.updateLoanApplicationStatus(loanId, newStatus).subscribe((response) => {
        expect(response).toEqual(updatedLoan);
        expect(response.status).toBe(LoanStatus.APPROVED);
        done();
      });

      const req = httpMock.expectOne(`/api/loans/loan-application/${loanId}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({ status: newStatus });
      req.flush(updatedLoan);
    });
  });

  describe('deleteLoanApplication', () => {
    it('should send DELETE request to remove loan', (done) => {
      const loanId = 1;

      service.deleteLoanApplication(loanId).subscribe((response) => {
        expect(response).toBeTruthy();
        done();
      });

      const req = httpMock.expectOne(`/api/loans/loan-application/${loanId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush({ message: 'Loan deleted successfully' });
    });
  });

  describe('error handling', () => {
    it('should handle 401 error', (done) => {
      service.getLoanApplications().subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(401);
          done();
        }
      );

      const req = httpMock.expectOne('/api/loans/loan-applications');
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle 404 error', (done) => {
      service.getLoanApplication(999).subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(404);
          done();
        }
      );

      const req = httpMock.expectOne('/api/loans/loan-application/999');
      req.flush({ message: 'Not found' }, { status: 404, statusText: 'Not Found' });
    });

    it('should handle 500 server error', (done) => {
      service.getLoanApplications().subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(500);
          done();
        }
      );

      const req = httpMock.expectOne('/api/loans/loan-applications');
      req.flush({ message: 'Server error' }, { status: 500, statusText: 'Internal Server Error' });
    });
  });
});
