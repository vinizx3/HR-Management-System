import { Routes } from '@angular/router';
import { authGuard, hrGuard } from './core/guards/auth.guard';

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
          import('./features/hr/dashboard/dashboard').then(m => m.Dashboard)
      },
      {
        path: 'employee/dashboard',
        loadComponent: () =>
          import('./features/hr/dashboard/dashboard').then(m => m.Dashboard)
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
        redirectTo: 'employee/dashboard',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];