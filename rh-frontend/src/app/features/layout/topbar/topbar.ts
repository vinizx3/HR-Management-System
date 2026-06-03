import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-topbar',
  imports: [RouterLink, CommonModule],
  templateUrl: './topbar.html',
  styleUrl: './topbar.css',
})
export class Topbar implements OnInit{

  employeeName: string ='';
  role: string = '';
  unreadCount: number = 0;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProfile();
    this.loadUnreadCount();
  }

  private loadProfile(): void {
  this.http.get<any>('/api/employees/me').subscribe({
    next: (employee) => {
      this.employeeName = employee.name;
      this.role = employee.role === 'HR_MANAGER' ? 'RH Manager' : 'Funcionario';
    },
    error: (err) => {
      console.error('Erro ao carregar perfil na Topbar:', err);
      this.employeeName = 'Usuário';
      this.role = 'Fazer Login';
    }
  });
}

  private loadUnreadCount(): void {
    this.http.get<number>('/api/notifications/me/unread-count').subscribe({
      next: (count) => this.unreadCount = count
    });
  }
}
