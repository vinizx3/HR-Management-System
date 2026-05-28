import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "../services/auth.service";


export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  console.log('%c[AuthGuard] Verificando acesso...', 'color: #007bff; font-weight: bold;');
  console.log('Logado?:', authService.isLoggedIn());
  console.log('Token existe?:', !!authService.getToken());

  if (authService.isLoggedIn()) {
    return true;
  }

  console.warn('[AuthGuard] Não logado ou token expirou! Expulsando para /login');

  router.navigate(['/login']);
  return false;
}; 

export const hrGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    console.log('%c[hrGuard] Verificando permissões de RH...', 'color: #ffc107; font-weight: bold;');
  console.log('Role encontrada:', authService.getRole());
  console.log('É HR Manager?:', authService.isHrManager());

    if (authService.isLoggedIn() && authService.isHrManager()) {
        return true;
    }

    console.warn('[hrGuard] Usuário não é RH Manager! Redirecionando para /employee/dashboard');

    router.navigate(['/employee/dashboard'])
    return false;
};