import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { LoginRequest, RegisterRequest, LoginResponse, Role } from '../models/auth.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockLoginResponse: LoginResponse = {
    id: 1,
    email: 'test@example.com',
    role: Role.USER,
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

      const req = httpMock.expectOne(r => r.url.includes('auth/login'));
      expect(req.request.method).toBe('POST');
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

      const req = httpMock.expectOne(r => r.url.includes('auth/login'));
      req.flush(mockLoginResponse);
    });
  });

  describe('register', () => {
    it('should send register request', (done) => {
      const registerRequest: RegisterRequest = {
        email: 'newuser@example.com',
        password: 'password123'
      };

      service.register(registerRequest).subscribe((response) => {
        expect(response).toBeTruthy();
        done();
      });

      const req = httpMock.expectOne(r => r.url.includes('auth/register'));
      expect(req.request.method).toBe('POST');
      req.flush({ id: 2, email: 'newuser@example.com', role: Role.USER });
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
    it('should return true if user is logged in', (done) => {
      const loginRequest: LoginRequest = {
        email: 'test@example.com',
        password: 'password123'
      };

      service.login(loginRequest).subscribe(() => {
        expect(service.isAuthenticated).toBe(true);
        done();
      });

      const req = httpMock.expectOne(r => r.url.includes('auth/login'));
      req.flush(mockLoginResponse);
    });

    it('should return false if not logged in', () => {
      localStorage.clear();
      expect(service.isAuthenticated).toBe(false);
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
