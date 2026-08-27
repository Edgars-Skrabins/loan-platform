import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { LoginRequest, RegisterRequest, LoginResponse } from '../models/auth.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockLoginResponse: LoginResponse = {
    id: 1,
    email: 'test@example.com',
    role: 'CUSTOMER',
    token: 'mock-jwt-token'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('login', () => {
    it('should send login request and store token', (done) => {
      const loginRequest: LoginRequest = {
        email: 'test@example.com',
        password: 'password123'
      };

      service.login(loginRequest).subscribe((response) => {
        expect(response).toEqual(mockLoginResponse);
        expect(localStorage.getItem('token')).toBe('mock-jwt-token');
        done();
      });

      const req = httpMock.expectOne('/api/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(loginRequest);
      req.flush(mockLoginResponse);
    });

    it('should update current user after login', (done) => {
      const loginRequest: LoginRequest = {
        email: 'test@example.com',
        password: 'password123'
      };

      service.login(loginRequest).subscribe(() => {
        expect(service.currentUserValue?.email).toBe('test@example.com');
        done();
      });

      const req = httpMock.expectOne('/api/auth/login');
      req.flush(mockLoginResponse);
    });

    it('should emit user changes after login', (done) => {
      const loginRequest: LoginRequest = {
        email: 'test@example.com',
        password: 'password123'
      };

      service.currentUser$.subscribe((user) => {
        if (user) {
          expect(user.email).toBe('test@example.com');
          done();
        }
      });

      service.login(loginRequest).subscribe();

      const req = httpMock.expectOne('/api/auth/login');
      req.flush(mockLoginResponse);
    });
  });

  describe('register', () => {
    it('should send register request', (done) => {
      const registerRequest: RegisterRequest = {
        email: 'newuser@example.com',
        password: 'password123',
        confirmPassword: 'password123',
        monthlyIncome: 5000,
        employmentStatus: 'EMPLOYED',
        creditScore: 750
      };

      service.register(registerRequest).subscribe((response) => {
        expect(response).toBeTruthy();
        done();
      });

      const req = httpMock.expectOne('/api/auth/register');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(registerRequest);
      req.flush({ message: 'Registration successful' });
    });
  });

  describe('logout', () => {
    it('should clear token and current user', () => {
      localStorage.setItem('token', 'mock-token');
      service.logout();

      expect(localStorage.getItem('token')).toBeNull();
      expect(service.currentUserValue).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('should return true if user is logged in', () => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('currentUser', JSON.stringify(mockLoginResponse));
      
      const isAuth = service.isAuthenticated;
      expect(isAuth).toBe(true);
    });

    it('should return false if not logged in', () => {
      localStorage.clear();
      const isAuth = service.isAuthenticated;
      expect(isAuth).toBe(false);
    });
  });

  describe('getToken', () => {
    it('should return token from localStorage', () => {
      localStorage.setItem('token', 'mock-jwt-token');
      expect(service.getToken()).toBe('mock-jwt-token');
    });

    it('should return null if no token exists', () => {
      localStorage.clear();
      expect(service.getToken()).toBeNull();
    });
  });
});
