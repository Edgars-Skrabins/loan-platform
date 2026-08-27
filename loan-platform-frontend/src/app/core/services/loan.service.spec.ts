import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LoanService } from './loan.service';
import { CreateLoanApplicationRequest, LoanApplication, LoanStatus, UpdateLoanApplicationStatusRequest } from '../models/loan.model';

describe('LoanService', () => {
  let service: LoanService;
  let httpMock: HttpTestingController;

  const mockLoanApplication: LoanApplication = {
    id: 1,
    amount: 50000,
    termMonths: 60,
    interestRate: 5.5,
    status: LoanStatus.PENDING,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
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
        expect(response.id).toBe(1);
        expect(response.status).toBe(LoanStatus.PENDING);
        done();
      });

      const req = httpMock.expectOne(req => req.url.includes('loan-application') && !req.url.includes('loan-applications'));
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(createRequest);
      req.flush({ id: 1, status: LoanStatus.PENDING });
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

      const req = httpMock.expectOne(req => req.url.includes('loan-applications'));
      expect(req.request.method).toBe('GET');
      req.flush(mockLoans);
    });

    it('should handle empty loan list', (done) => {
      service.getLoanApplications().subscribe((response) => {
        expect(response).toEqual([]);
        done();
      });

      const req = httpMock.expectOne(req => req.url.includes('loan-applications'));
      req.flush([]);
    });
  });

  describe('getLoanApplication', () => {
    it('should fetch single loan application by id', (done) => {
      service.getLoanApplication(1).subscribe((response) => {
        expect(response.id).toBe(1);
        expect(response.status).toBe(LoanStatus.PENDING);
        done();
      });

      const req = httpMock.expectOne(req => req.url.includes('loan-application/1'));
      expect(req.request.method).toBe('GET');
      req.flush(mockLoanApplication);
    });
  });

  describe('updateLoanApplicationStatus', () => {
    it('should send PUT request to update loan status', (done) => {
      const updateRequest: UpdateLoanApplicationStatusRequest = {
        id: 1,
        newStatus: LoanStatus.APPROVED
      };

      service.updateLoanApplicationStatus(updateRequest).subscribe((response) => {
        expect(response.newStatus).toBe(LoanStatus.APPROVED);
        done();
      });

      const req = httpMock.expectOne(req => req.url.includes('loan-application/1'));
      expect(req.request.method).toBe('PUT');
      req.flush({ id: 1, newStatus: LoanStatus.APPROVED });
    });
  });

  describe('deleteLoanApplication', () => {
    it('should send DELETE request to remove loan', (done) => {
      service.deleteLoanApplication(1).subscribe(() => {
        done();
      });

      const req = httpMock.expectOne(req => req.url.includes('loan-application/1'));
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });

  describe('error handling', () => {
    it('should propagate 401 errors', (done) => {
      service.getLoanApplications().subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(401);
          done();
        }
      );

      const req = httpMock.expectOne(req => req.url.includes('loan-applications'));
      req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    });

    it('should propagate 404 errors', (done) => {
      service.getLoanApplication(999).subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(404);
          done();
        }
      );

      const req = httpMock.expectOne(req => req.url.includes('loan-application/999'));
      req.flush({ message: 'Not found' }, { status: 404, statusText: 'Not Found' });
    });

    it('should propagate 500 server errors', (done) => {
      service.getLoanApplications().subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(500);
          done();
        }
      );

      const req = httpMock.expectOne(req => req.url.includes('loan-applications'));
      req.flush({ message: 'Server error' }, { status: 500, statusText: 'Internal Server Error' });
    });
  });
});
