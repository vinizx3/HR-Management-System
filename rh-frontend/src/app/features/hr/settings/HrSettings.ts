import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

interface Profile {
  id: string;
  name: string;
  email: string;
  role: string;
  department: string;
  salary: number;
}

@Component({
  selector: 'app-hr-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './hr-settings.html',
  styleUrl: './hr-settings.css'
})
export class HrSettings implements OnInit {

  profile: Profile | null = null;
  isLoading = true;
  isEditing = false;
  isSaving = false;
  saveSuccess = false;
  saveError = '';

  editForm = {
    name: '',
    department: ''
  };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.http.get<Profile>('/api/employees/me').subscribe({
      next: (data) => {
        this.profile = data;
        this.editForm.name = data.name;
        this.editForm.department = data.department;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
  }

  getRoleLabel(role: string): string {
    return role === 'HR_MANAGER' ? 'RH Manager' : 'Funcionário';
  }

  getSystemInfo(): { label: string; value: string }[] {
    return [
      { label: 'Versão do sistema', value: 'v1.0.0' },
      { label: 'Backend', value: 'Java 21 + Spring Boot 3' },
      { label: 'Banco de dados', value: 'PostgreSQL' },
      { label: 'Mensageria', value: 'Apache Kafka' },
      { label: 'Autenticação', value: 'JWT + Spring Security' },
    ];
  }
}