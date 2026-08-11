import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

interface PendingItem {
  id: string;
  employeeName: string;
  type: 'Férias' | 'Ajuste de ponto';
  typeColor: string;
  detail: string;
}

@Component({
  selector: 'app-hr-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
})
export class HrDashboard implements OnInit {

  managerName: string = '';
  today = new Date();

  totalEmployees: number = 0;
  pendingAdjustments: number = 0;
  pendingVacations: number = 0;
  expiringVacations: number = 0;

  pendingList: PendingItem[] = [];
  isLoading: boolean = true;
  

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.pendingList = [];
    this.isLoading = true;

    this.http.get<any>('/api/employees/me').subscribe({
      next: (employee) => this.managerName = employee.name
    });

    this.http.get<any[]>('/api/employees').subscribe({
      next: (employees) => this.totalEmployees = employees.length
    });

    this.http.get<any[]>('/api/vacations/expiring').subscribe({
      next: (vacations) => this.expiringVacations = vacations.length
    });

    this.http.get<any[]>('/api/vacations/pending').subscribe({
      next: (vacations) => {
        this.pendingVacations = vacations.length;

        const vacationItems: PendingItem[] = vacations.map(v => ({
          id: v.id,
          employeeName: v.employeeName,
          type: 'Férias',
          typeColor: '#10B981',
          detail: `${v.startDate} a ${v.endDate}`
        }));

        this.pendingList = [...this.pendingList, ...vacationItems];
        this.checkLoading();
      }
    });

    this.http.get<any[]>('/api/timeclock/adjustment/pending').subscribe({
      next: (adjustments) => {
        this.pendingAdjustments = adjustments.length;

        const adjustmentItems: PendingItem[] = adjustments.map(a => ({
          id: a.id,
          employeeName: a.employeeName,
          type: 'Ajuste de ponto',
          typeColor: '#F59E0B',
          detail: `${a.requestedClockIn} → ${a.requestedClockOut}`
        }));

        this.pendingList = [...this.pendingList, ...adjustmentItems];
        this.checkLoading();
      }
    });
  }

  private requestsCompleted = 0;

  private checkLoading(): void {
    this.requestsCompleted++;

    if (this.requestsCompleted >= 2) {
      this.isLoading = false;
    }
  }

  approve(item: PendingItem): void {
    const endpoint = item.type === 'Férias'
      ? `/api/vacations/${item.id}/approve`
      : `/api/timeclock/adjustment/${item.id}/approve`;

    this.http.put(endpoint, {}).subscribe({
      next: () => this.loadDashboardData()
    });
  }

  reject(item: PendingItem): void {
    const endpoint = item.type === 'Férias'
      ? `/api/vacations/${item.id}/reject`
      : `/api/timeclock/adjustment/${item.id}/reject`;

    this.http.put(endpoint, {}).subscribe({
      next: () => this.loadDashboardData()
    });
  }

  getGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'Bom dia';
    if (hour < 18) return 'Boa tarde';
    return 'Boa noite';
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
  }

  get isDemo(): boolean {
  return this.authService.isDemoAccount();
  }
}