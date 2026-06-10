import { HttpInterceptorFn } from '@angular/common/http';

// Production API URL
const API_URL = 'https://hr-management-system-4smu.onrender.com';

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.startsWith('/api')) {
    const apiReq = req.clone({
      url: `${API_URL}${req.url}`
    });
    return next(apiReq);
  }
  return next(req);
};