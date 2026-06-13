import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

interface TimeRecord {
  id: string;
  employeeName: string;
  date: string;
  clockIn: string;
  clockOut: string | null;
  workedMinutes: number;
  workedTime: string | null;
  overtimeMinutes: number;
  overtimeTime: string;
  status: 'OPEN' | 'CLOSED' | 'ADJUSTED';
}

interface AdjustmentForm {
  timeRecordId: string;
  requestedClockIn: string;
  requestedClockOut: string;
  reason: string;
}

@Component({
  selector: 'app-timeclock',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './timeclock.html',
  styleUrl: './timeclock.css',
})
export class Timeclock implements OnInit, OnDestroy {

  records: TimeRecord[] = [];
  isLoading = true;
  isClockedIn = false;
  isClockingIn = false;
  isHrManager = false;
  currentTime = '';
  currentDate = '';
  private clockInterval: any;

  showAdjustModal = false;
  isSubmittingAdjust = false;
  adjustError = '';
  adjustSuccess = '';

  adjustForm: AdjustmentForm = {
    timeRecordId: '',
    requestedClockIn: '',
    requestedClockOut: '',
    reason: ''
  };

  selectedRecord: TimeRecord | null = null;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.isHrManager = this.authService.isHrManager();
    this.startClock();
    this.loadRecords();
  }

  ngOnDestroy(): void {
    clearInterval(this.clockInterval);
  }

  private startClock(): void {
    this.updateClock();
    this.clockInterval = setInterval(() => this.updateClock(), 1000);
  }

  private updateClock(): void {
    const now = new Date();
    this.currentTime = now.toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
    this.currentDate = now.toLocaleDateString('pt-BR', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    }).toLowerCase().replace(/^\w/, c => c.toUpperCase());
  }

  loadRecords(): void {
    const endpoint = this.isHrManager
      ? '/api/timeclock/all'
      : '/api/timeclock/me';

    this.http.get<TimeRecord[]>(endpoint).subscribe({
      next: (data) => {
        this.records = data.reverse();

        const now = new Date();
        const day = String(now.getDate()).padStart(2, '0');
        const month = String(now.getMonth() + 1).padStart(2, '0'); // Mês começa em 0
        const year = now.getFullYear();
        const today = `${day}/${month}/${year}`; // Resultado exato: "13/06/2026"

        // Garante que a comparação por string seja idêntica ao DTO
        this.isClockedIn =
          this.records.length > 0 &&
          this.records[0].status === 'OPEN' &&
          this.records[0].date === today;

        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }


  clockAction(): void {
    if (this.isClockingIn) return;
    this.isClockingIn = true;

    const endpoint = this.isClockedIn
      ? '/api/timeclock/clock-out'
      : '/api/timeclock/clock-in';

    this.http.post<TimeRecord>(endpoint, {}).subscribe({
      next: () => {
        this.loadRecords();
        this.isClockingIn = false;
      },
      error: (err) => {
        this.adjustError = err.error?.message || 'Erro ao registrar ponto do administrador.';
        this.isClockingIn = false;
      }
    });
  }

  openAdjustModal(record: TimeRecord): void {
    if (!record || !record.date) {
      this.adjustError = 'Dados do registro inválidos ou incompletos.';
      return;
    }

    this.selectedRecord = record;
    this.adjustError = '';
    this.adjustSuccess = '';

    let dateStr = '';
    if (record.date.includes('/')) {
      const [day, month, year] = record.date.split('/');
      dateStr = `${year}-${month}-${day}`;
    } else {
      dateStr = record.date.substring(0, 10); // Assume yyyy-MM-dd
    }

    const defaultClockIn = record.clockIn ? record.clockIn : '08:00';
    const defaultClockOut = record.clockOut ? record.clockOut : '17:00';

    this.adjustForm = {
      timeRecordId: record.id,
      requestedClockIn: `${dateStr}T${defaultClockIn}`,
      requestedClockOut: `${dateStr}T${defaultClockOut}`,
      reason: ''
    };

    this.showAdjustModal = true;
  }

  closeAdjustModal(): void {
    this.showAdjustModal = false;
    this.adjustError = '';
    this.adjustSuccess = '';
    this.selectedRecord = null;
  }

  submitAdjustment(): void {
    if (!this.adjustForm.reason.trim()) {
      this.adjustError = 'O motivo é obrigatório.';
      return;
    }
    if (!this.adjustForm.requestedClockIn || !this.adjustForm.requestedClockOut) {
      this.adjustError = 'Preencha os horários de entrada e saída.';
      return;
    }

    this.isSubmittingAdjust = true;
    this.adjustError = '';

    let clockInBody = this.adjustForm.requestedClockIn;
    let clockOutBody = this.adjustForm.requestedClockOut;

    if ((clockInBody.match(/:/g) || []).length === 1) clockInBody += ':00';
    if ((clockOutBody.match(/:/g) || []).length === 1) clockOutBody += ':00';

    const body = {
      timeRecordId: this.adjustForm.timeRecordId,
      requestedClockIn: clockInBody,
      requestedClockOut: clockOutBody,
      reason: this.adjustForm.reason
    };

    this.http.post('/api/timeclock/adjustment', body).subscribe({
      next: () => {
        this.adjustSuccess = 'Solicitação enviada com sucesso!';
        this.isSubmittingAdjust = false;
        setTimeout(() => this.closeAdjustModal(), 1500);
      },
      error: (err) => {
        this.adjustError = err.error?.message || 'Erro ao enviar solicitação.';
        this.isSubmittingAdjust = false;
      }
    });
  }

  getStatusLabel(status: string): string {
    if (status === 'CLOSED') return 'Completo';
    if (status === 'OPEN') return 'Em andamento';
    if (status === 'ADJUSTED') return 'Ajustado';
    return status;
  }

  getStatusClass(status: string): string {
    if (status === 'CLOSED') return 'badge-success';
    if (status === 'OPEN') return 'badge-info';
    if (status === 'ADJUSTED') return 'badge-warning';
    return '';
  }
}