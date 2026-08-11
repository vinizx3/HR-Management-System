import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

interface Vacation {
  id: string;
  employeeName: string;
  startDate: string;
  endDate: string;
  vacationDays: number;
  vacationStatus: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  requestedAt: string;
}

@Component({
  selector: 'app-vacations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vacations.html',
  styleUrl: './vacations.css',
})
export class Vacations implements OnInit{

  isHrManager = false;
  vacations: Vacation[] = [];
  isLoading = true;
  isSubmitting = false;

  startDate = '';
  endDate = '';
  previewDays: number | null = null;
  formError = '';
  formSuccess = '';

  minDate = '';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.isHrManager = this.authService.isHrManager();
    const min = new Date();
    min.setDate(min.getDate() + 30);
    this.minDate = min.toISOString().split('T')[0];
    this.loadVacations();
  }

  loadVacations(): void {
    const endpoint = this.isHrManager ? '/api/vacations/all' : '/api/vacations/me';
    this.http.get<Vacation[]>(endpoint).subscribe({
      next: (data) => {
        this.vacations = data;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  onDatesChange(): void {
    this.formError = '';
    if (this.startDate && this.endDate) {
      const start = new Date(this.startDate);
      const end = new Date(this.endDate);
      const diff = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
      if (diff < 1) {
        this.previewDays = null;
        this.formError = 'A data final deve ser após a data inicial.';
      } else if (diff > 30) {
        this.previewDays = null;
        this.formError = 'O período não pode exceder 30 dias.';
      } else {
        this.previewDays = diff;
      }
    } else {
      this.previewDays = null;
    }
  }

  submitRequest(): void {
    if (!this.startDate || !this.endDate) {
      this.formError = 'Preencha as datas de início e fim.';
      return;
    }
    if (this.formError) return;

    this.isSubmitting = true;
    this.formSuccess = '';
    this.formError = '';

    this.http.post<Vacation>('/api/vacations/request', {
      startDate: this.startDate,
      endDate: this.endDate
    }).subscribe({
      next: () => {
        this.formSuccess = 'Solicitação enviada com sucesso!';
        this.startDate = '';
        this.endDate = '';
        this.previewDays = null;
        this.isSubmitting = false;
        this.loadVacations();
      },
      error: (err) => {
        this.formError = err.error?.message || 'Erro ao enviar solicitação.';
        this.isSubmitting = false;
      }
    });
  }

  approve(id: string): void {
    this.http.put(`/api/vacations/${id}/approve`, {}).subscribe({
      next: () => this.loadVacations()
    });
  }

  reject(id: string): void {
    this.http.put(`/api/vacations/${id}/reject`, {}).subscribe({
      next: () => this.loadVacations()
    });
  }

  getStatusLabel(status: string): string {
    const map: any = {
      PENDING: 'Pendente',
      APPROVED: 'Aprovada',
      REJECTED: 'Rejeitada',
      CANCELLED: 'Cancelada'
    };
    return map[status] || status;
  }

  getStatusClass(status: string): string {
    const map: any = {
      PENDING: 'badge-warning',
      APPROVED: 'badge-success',
      REJECTED: 'badge-danger',
      CANCELLED: 'badge-neutral'
    };
    return map[status] || '';
  }

  getReturnDate(): string {
    if (!this.endDate) return '';
    const end = new Date(this.endDate);
    end.setDate(end.getDate() + 1);
    return end.toLocaleDateString('pt-BR');
  }

  get isDemo(): boolean {
  return this.authService.isDemoAccount();
  }
}
