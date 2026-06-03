import { Router, Routes } from '@angular/router';
import { authGuard, hrGuard } from './core/guards/auth.guard';
import { inject } from '@angular/core';
import { AuthService } from './core/services/auth.service';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login').then(m => m.Login)
  },
  {
    path: '',
    loadComponent: () =>
      import('./features/layout/shell/shell').then(m => m.Shell),
    canActivate: [authGuard],
    children: [
      {
        path: 'hr/dashboard',
        canActivate: [hrGuard],
        loadComponent: () =>
          import('./features/employee/dashboard/EmployeeDashboard').then(m => m.EmployeeDashboard)
      },
      {
        path: 'employee/dashboard',
        loadComponent: () =>
          import('./features/employee/dashboard/EmployeeDashboard').then(m => m.EmployeeDashboard)
      },
      {
        path: 'timeclock',
        loadComponent: () =>
          import('./features/timeclock/timeclock/timeclock').then(m => m.Timeclock)
      },
      {
        path: 'vacations',
        loadComponent: () =>
          import('./features/vacations/vacations/vacations').then(m => m.Vacations)
      },
      {
        path: 'notifications',
        loadComponent: () =>
          import('./features/notifications/notifications/notifications').then(m => m.Notifications)
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: () => {
          const authService = inject(AuthService);
          return authService.isHrManager() ? 'hr/dashboard' : 'employee/dashboard';
        }
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];