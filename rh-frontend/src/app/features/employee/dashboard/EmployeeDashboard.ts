import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboards.html',
  styleUrls: ['./dashboards.css']
})
export class EmployeeDashboard implements OnInit, OnDestroy {
  employeeName: string = '';
  todayRecord: any = null;
  overtimeBalance: any = null;
  nextVacation: any = null;
  weekRecords: any[] = [];
  isClockedIn: boolean = false;
  isLoading: boolean = true; 

  today = new Date();

  private timer: any;
  private now: Date = new Date();

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {console.log('EmployeeDashboard carregado');}

  ngOnInit(): void {
  this.loadDashboardData();
  this.startLiveTimer();
  }

  private startLiveTimer(): void {
    this.timer = setInterval(() => {
      this.now = new Date();
      this.cdr.detectChanges(); 
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  loadDashboardData(): void {
    this.isLoading = true;

    this.http.get<any>('/api/employees/me').subscribe({
      next: (employee) => {
        this.employeeName = employee.name;
      }
    });

    this.http.get<any[]>('/api/timeclock/me').subscribe({
      next: (records) => {

        if (records && records.length > 0) {
          const orderedRecords = [...records].reverse();
          const todayStr = this.formatDate(new Date());

          this.weekRecords = orderedRecords.slice(0, 7);

          this.todayRecord =
            orderedRecords.find(r => r.date === todayStr) || null;

          this.isClockedIn =
            this.todayRecord?.status === 'OPEN' &&
            this.todayRecord?.date === todayStr;

        } else {
          this.weekRecords = [];
          this.todayRecord = null;
          this.isClockedIn = false;
        }
      },
      error: () => {
        this.weekRecords = [];
      },
      complete: () => {
        this.isLoading = false;
      }
    });

    this.http.get<any>('/api/overtime/me').subscribe({
      next: (balance) => this.overtimeBalance = balance
    });

    this.http.get<any[]>('/api/vacations/me').subscribe({
      next: (vacations) => {
        if (vacations) {
          this.nextVacation =
            vacations.find(v => v.vacationStatus === 'APPROVED') || null;
        }
      }
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

  getWorkedMinutes(): number {
  if (!this.todayRecord) return 0;

  const clockIn = new Date(this.todayRecord.clockIn);

  if (this.todayRecord.status === 'CLOSED') {
    return this.todayRecord.workedMinutes ?? 0;
  }

  return Math.floor((this.now.getTime() - clockIn.getTime()) / 60000);
}

  formatMinutes(minutes: number): string {
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return `${h}h ${m}min`;
  }

  getGreeting(): string {
    const hour = new Date().getHours();

    if (hour < 12) return 'Bom dia';
    if (hour < 18) return 'Boa tarde';
    return 'Boa noite';
  }

  getProgressPercent(): number {
  const minutes = this.getWorkedMinutes();
  return Math.min((minutes / 480) * 100, 100);
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

  private formatDate(date: Date): string {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    return `${day}/${month}/${year}`;
  }

}
