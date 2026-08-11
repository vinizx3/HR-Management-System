import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

interface Employee {
  id: string;
  name: string;
  email: string;
  role: 'HR_MANAGER' | 'EMPLOYEE';
  department: string;
  salary: number;
  active: boolean;
}

interface EmployeeForm {
  name: string;
  email: string;
  password: string;
  role: 'HR_MANAGER' | 'EMPLOYEE';
  department: string;
  salary: number | null;
}

@Component({
  selector: 'app-hr-employees',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './hr-employees.html',
  styleUrl: './hr-employees.css'
})
export class HrEmployees implements OnInit {

  employees: Employee[] = [];
  isLoading = true;
  isSubmitting = false;

  showModal = false;
  isEditing = false;
  editingId: string | null = null;

  showDeactivateConfirm = false;
  deactivatingEmployee: Employee | null = null;

  formError = '';
  formSuccess = '';

  searchTerm = '';

  form: EmployeeForm = this.emptyForm();

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.isLoading = true;
    this.http.get<Employee[]>('/api/employees').subscribe({
      next: (data) => {
        this.employees = data;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  get filteredEmployees(): Employee[] {
    if (!this.searchTerm.trim()) return this.employees;
    const term = this.searchTerm.toLowerCase();
    return this.employees.filter(e =>
      e.name.toLowerCase().includes(term) ||
      e.email.toLowerCase().includes(term) ||
      e.department?.toLowerCase().includes(term)
    );
  }

  openCreateModal(): void {
    this.isEditing = false;
    this.editingId = null;
    this.form = this.emptyForm();
    this.formError = '';
    this.formSuccess = '';
    this.showModal = true;
  }

  openEditModal(employee: Employee): void {
    this.isEditing = true;
    this.editingId = employee.id;
    this.form = {
      name: employee.name,
      email: employee.email,
      password: '',
      role: employee.role,
      department: employee.department,
      salary: employee.salary
    };
    this.formError = '';
    this.formSuccess = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.formError = '';
    this.formSuccess = '';
  }

  submitForm(): void {
    if (!this.form.name || !this.form.email || !this.form.salary) {
      this.formError = 'Preencha todos os campos obrigatórios.';
      return;
    }
    if (!this.isEditing && !this.form.password) {
      this.formError = 'A senha é obrigatória para novos funcionários.';
      return;
    }

    this.isSubmitting = true;
    this.formError = '';

    const request$ = this.isEditing
      ? this.http.put<Employee>(`/api/employees/${this.editingId}`, this.form)
      : this.http.post<Employee>('/api/employees', this.form);

    request$.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.closeModal();
        this.loadEmployees();
      },
      error: (err) => {
        this.formError = err.error?.message || 'Erro ao salvar funcionário.';
        this.isSubmitting = false;
      }
    });
  }

  confirmDeactivate(employee: Employee): void {
    this.deactivatingEmployee = employee;
    this.showDeactivateConfirm = true;
  }

  cancelDeactivate(): void {
    this.deactivatingEmployee = null;
    this.showDeactivateConfirm = false;
  }

  deactivate(): void {
    if (!this.deactivatingEmployee) return;
    this.http.delete(`/api/employees/${this.deactivatingEmployee.id}`).subscribe({
      next: () => {
        this.showDeactivateConfirm = false;
        this.deactivatingEmployee = null;
        this.loadEmployees();
      }
    });
  }

  getRoleLabel(role: string): string {
    return role === 'HR_MANAGER' ? 'RH Manager' : 'Funcionário';
  }

  getRoleClass(role: string): string {
    return role === 'HR_MANAGER' ? 'badge-primary' : 'badge-neutral';
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
  }

  get isDemo(): boolean {
  return this.authService.isDemoAccount();
  }

  private emptyForm(): EmployeeForm {
    return {
      name: '',
      email: '',
      password: '',
      role: 'EMPLOYEE',
      department: '',
      salary: null
    };
  }
}