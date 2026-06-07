import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

interface Notification {
  id: string;
  message: string;
  read: boolean;
  createdAt: string;
  }

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css',
})
export class Notifications implements OnInit {

  notifications: Notification[] = [];
  filteredNotifications: Notification[] = [];

  activeFilter: 'todas' | 'nao-lidas' | 'ferias' | 'ponto' = 'todas';
  isLoading = true;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.http.get<Notification[]>('/api/notifications/me').subscribe({
      next: (data) => {
        this.notifications = data;
        this.applyFilter(this.activeFilter);
        this.isLoading = false;      
        this.markAllAsRead();
      },
      error: () => this.isLoading = false
    });
  }

  applyFilter(filter: 'todas' | 'nao-lidas' | 'ferias' | 'ponto'): void {
    this.activeFilter = filter;
    
    switch (filter) {
      case 'nao-lidas':
        this.filteredNotifications = this.notifications.filter(n => !n.read);
        break;
      case 'ferias':
        this.filteredNotifications = this.notifications.filter(n => 
          n.message.toLowerCase().includes('vacation') || 
          n.message.toLowerCase().includes('férias')
        );
        break;
      case 'ponto':
        this.filteredNotifications = this.notifications.filter(n =>
          n.message.toLowerCase().includes('adjustment') ||
          n.message.toLowerCase().includes('ponto') || 
          n.message.toLowerCase().includes('clock')
        );
        break;
      default:
        this.filteredNotifications = this.notifications;
        break;
    }
  }

  markAllAsRead(): void {
    this.http.patch('/api/notifications/me/read-all', {}).subscribe({
      next: () => {
        this.notifications = this.notifications.map(n => ({ ...n, read: true }));
        this.applyFilter(this.activeFilter);
      }
    });
  }

  getIcon(message: string): string {
    const msg = message.toLowerCase();
    if (msg.includes('vacation')) return 'bi-umbrella-fill';
    if (msg.includes('adjustment')) return 'bi-clock-fill';
    return 'bi-bell-fill';
  }


  getIconBg(message: string): string {
    const msg = message.toLowerCase();
    if (msg.includes('approved')) return 'icon-success';
    if (msg.includes('rejected')) return 'icon-danger';
    return 'icon-info';
  }


  hasUnread(): boolean {
    return this.notifications.some(n => !n.read);
  }

  translateMessage(message: string): string {
  const msg = message.toLowerCase();
    if (msg.includes('vacation') && msg.includes('approved'))
    return 'Sua solicitação de férias foi aprovada! 🎉';
    if (msg.includes('vacation') && msg.includes('rejected'))
    return 'Sua solicitação de férias foi rejeitada.';
    if (msg.includes('adjustment') && msg.includes('approved'))
    return 'Seu ajuste de ponto foi aprovado! ✅';
    if (msg.includes('adjustment') && msg.includes('rejected'))
    return 'Seu ajuste de ponto foi rejeitado.';
  return message;
}
}
