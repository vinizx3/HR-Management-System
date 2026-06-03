import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboards.html',
  styleUrls: ['./dashboards.css']
})
export class EmployeeDashboard implements OnInit {
  employeeName: string = '';
  todayRecord: any = null;
  overtimeBalance: any = null;
  nextVacation: any = null;
  weekRecords: any[] = [];
  isClockedIn: boolean = false;
  isLoading: boolean = true; 

  constructor(private http: HttpClient) {console.log('EmployeeDashboard carregado');}

  today = new Date();

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.http.get<any>('/api/employees/me').subscribe({
      next: (employee) => {
        this.employeeName = employee.name;
      },
      error: (err) => console.error('Erro ao buscar perfil do funcionário:', err)
    });

  this.http.get<any[]>('/api/timeclock/me').subscribe({
      next: (records) => {
        if (records && records.length > 0) {
          this.weekRecords = records.slice(0, 7);
          this.todayRecord = records[0] || null;
          this.isClockedIn = this.todayRecord?.status === 'OPEN';
        } else {
          this.weekRecords = [];
          this.todayRecord = null;
          this.isClockedIn = false;
        }
      },
      error: (err) => {
        console.error('Erro ao carregar o ponto (provavelmente perfil de RH):', err);
        this.weekRecords = [];
      }, complete: () => {
        this.isLoading = false; 
      }
    });
      
    this.http.get<any>('/api/overtime/me').subscribe({
      next: (balance) => this.overtimeBalance = balance,
      error: (err) => console.error('Erro ao buscar banco de horas:', err)
    });

    this.http.get<any[]>('/api/vacations/me').subscribe({
      next: (vacations) => {
        if (vacations) {
          this.nextVacation = vacations.find(v => v.vacationStatus === 'APPROVED') || null;
        }
      },
      error: (err) => console.error('Erro ao buscar férias:', err)
    });
  }

    clockIn(): void  {
      this.http.post<any>('/api/timeclock/clock-in', {}).subscribe({
        next: () => this.loadDashboardData()
      });
    }

    clockOut(): void {
      this.http.post<any>('/api/timeclock/clock-out', {}).subscribe({
        next: () => this.loadDashboardData()
      });
    }

    getGreeting(): string {
      const hour = new Date().getHours();
      if (hour < 12) return 'Bom dia';
      if (hour < 18) return 'Boa tarde';
      return 'Boa noite';
    }

    getProgressPercent(): number {
      if (!this.todayRecord) return 0;
      return Math.min(((
        this.todayRecord.workedminutes ?? 0) / 480) * 100,100);
    }

    getBadgeClass(status: string): string {
      const map: any = {
        'CLOSED': 'badge-success',
        'OPEN': 'badge-info',
        'ADJUSTED': 'badge-warning'
      };
      return map[status] || 'badge-secondary';
    }

    getStatusLabel(status: string): string {
      const map: any = {
        'CLOSED': 'Completo',
        'OPEN': 'Em andamento',
        'ADJUSTED': 'Ajustado'
      };
      return map[status] || status;
    }
}
