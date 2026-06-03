import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-hr-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
})

export class HrDashboard implements OnInit {
  employeeName: string = '';
  todayRecord: any = null;
  overtimeBalance: any = null;
  nextVacation: any = null;
  weekRecords: any[] = [];
  isClockedIn: boolean = false;
  isLoading: boolean = true; 

  constructor(private http: HttpClient) {}

  today = new Date();

  ngOnInit(): void {}
}
