import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="shell">
      <aside class="sidebar">
        <div class="brand">CMS UCSal</div>
        <nav>
          <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
          <a routerLink="/users" routerLinkActive="active">Usuários</a>
          <a routerLink="/courses" routerLinkActive="active">Cursos</a>
          <a routerLink="/news" routerLinkActive="active">Notícias</a>
          <a routerLink="/articles" routerLinkActive="active">Artigos</a>
          <a routerLink="/projects" routerLinkActive="active">Projetos</a>
          <a routerLink="/events" routerLinkActive="active">Eventos</a>
        </nav>
        <button class="logout" (click)="logout()">Sair</button>
      </aside>
      <main class="content">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [`
    .shell { display: flex; height: 100vh; overflow: hidden; }
    .sidebar { width: 220px; background: #1a237e; color: #fff; display: flex; flex-direction: column; padding: 1.5rem 0; flex-shrink: 0; }
    .brand { font-size: 1.2rem; font-weight: 700; padding: 0 1.5rem 2rem; letter-spacing: .5px; }
    nav { display: flex; flex-direction: column; gap: .25rem; flex: 1; }
    nav a { padding: .75rem 1.5rem; color: rgba(255,255,255,.75); text-decoration: none; font-size: .9rem; transition: background .15s; }
    nav a:hover, nav a.active { background: rgba(255,255,255,.15); color: #fff; }
    .logout { margin: 1rem 1.5rem 0; padding: .6rem; background: rgba(255,255,255,.1); color: #fff; border: 1px solid rgba(255,255,255,.3); border-radius: 8px; cursor: pointer; }
    .logout:hover { background: rgba(255,255,255,.2); }
    .content { flex: 1; overflow-y: auto; background: #f0f2f5; }
  `]
})
export class LayoutComponent {
  constructor(private auth: AuthService, private router: Router) {}
  logout(): void { this.auth.logout(); this.router.navigate(['/login']); }
}
