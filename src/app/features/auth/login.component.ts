import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <h1>CMS UCSal</h1>
        <p class="subtitle">Gerenciador de Conteúdo</p>
        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="field">
            <label>E-mail</label>
            <input type="email" formControlName="email" placeholder="seu@email.com" />
          </div>
          <div class="field">
            <label>Senha</label>
            <input type="password" formControlName="password" placeholder="••••••••" />
          </div>
          <p *ngIf="error" class="error">{{ error }}</p>
          <button type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .login-container { display:flex; justify-content:center; align-items:center; height:100vh; background:#f0f2f5; }
    .login-card { background:#fff; padding:2.5rem; border-radius:12px; box-shadow:0 4px 20px rgba(0,0,0,.1); width:360px; }
    h1 { margin:0; color:#1a237e; font-size:1.8rem; }
    .subtitle { color:#666; margin:.25rem 0 2rem; }
    .field { margin-bottom:1.2rem; display:flex; flex-direction:column; gap:.4rem; }
    label { font-weight:600; font-size:.9rem; color:#333; }
    input { padding:.7rem 1rem; border:1px solid #ddd; border-radius:8px; font-size:1rem; }
    input:focus { outline:none; border-color:#1a237e; }
    button { width:100%; padding:.85rem; background:#1a237e; color:#fff; border:none; border-radius:8px; font-size:1rem; cursor:pointer; }
    button:hover:not(:disabled) { background:#283593; }
    button:disabled { opacity:.6; cursor:default; }
    .error { color:#e53935; font-size:.85rem; margin:.5rem 0; }
  `]
})
export class LoginComponent {
  form: FormGroup;
  error = '';
  loading = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private auth: AuthService,
    private userService: UserService
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    this.loading = true;
    this.error = '';
    this.userService.getAll().subscribe({
      next: (users) => {
        const user = users.find(
          u => u.email === this.form.value.email && (u as any).password === this.form.value.password
        );
        if (user) {
          this.auth.login(user);
          this.router.navigate(['/dashboard']);
        } else {
          this.error = 'E-mail ou senha inválidos.';
        }
        this.loading = false;
      },
      error: () => {
        this.error = 'Erro ao conectar com o servidor.';
        this.loading = false;
      }
    });
  }
}
