import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { CourseService } from '../../core/services/course.service';
import { NewsService } from '../../core/services/news.service';
import { EventService } from '../../core/services/event.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="dashboard">
      <h2>Bem-vindo, {{ user?.name }}</h2>
      <p class="role">Perfil: <strong>{{ user?.role }}</strong></p>
      <div class="cards">
        <div class="card" routerLink="/users">
          <span class="icon">👥</span>
          <span class="count">{{ counts.users }}</span>
          <span class="label">Usuários</span>
        </div>
        <div class="card" routerLink="/courses">
          <span class="icon">📚</span>
          <span class="count">{{ counts.courses }}</span>
          <span class="label">Cursos</span>
        </div>
        <div class="card" routerLink="/news">
          <span class="icon">📰</span>
          <span class="count">{{ counts.news }}</span>
          <span class="label">Notícias</span>
        </div>
        <div class="card" routerLink="/events">
          <span class="icon">📅</span>
          <span class="count">{{ counts.events }}</span>
          <span class="label">Eventos</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard { padding:2rem; }
    h2 { margin:0 0 .25rem; color:#1a237e; }
    .role { color:#666; margin:0 0 2rem; }
    .cards { display:grid; grid-template-columns:repeat(auto-fill,minmax(180px,1fr)); gap:1.5rem; }
    .card { background:#fff; border-radius:12px; padding:1.5rem; box-shadow:0 2px 8px rgba(0,0,0,.08); cursor:pointer; display:flex; flex-direction:column; align-items:center; gap:.5rem; transition:transform .15s; }
    .card:hover { transform:translateY(-3px); box-shadow:0 6px 16px rgba(0,0,0,.12); }
    .icon { font-size:2rem; }
    .count { font-size:2rem; font-weight:700; color:#1a237e; }
    .label { font-size:.9rem; color:#555; }
  `]
})
export class DashboardComponent implements OnInit {
  counts = { users: 0, courses: 0, news: 0, events: 0 };

  constructor(
    public auth: AuthService,
    private userService: UserService,
    private courseService: CourseService,
    private newsService: NewsService,
    private eventService: EventService
  ) {}

  get user() { return this.auth.currentUser(); }

  ngOnInit(): void {
    this.userService.getAll().subscribe(d => this.counts.users = d.length);
    this.courseService.getAll().subscribe(d => this.counts.courses = d.length);
    this.newsService.getAll().subscribe(d => this.counts.news = d.length);
    this.eventService.getAll().subscribe(d => this.counts.events = d.length);
  }
}
