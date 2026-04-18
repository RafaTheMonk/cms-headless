import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { User, UserRole } from '../../core/models/user.model';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page">
      <div class="header">
        <h2>Usuários</h2>
        <button class="btn-primary" (click)="openForm()">+ Novo Usuário</button>
      </div>

      <div class="modal-overlay" *ngIf="showForm" (click)="closeForm()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>{{ editing ? 'Editar' : 'Novo' }} Usuário</h3>
          <form [formGroup]="form" (ngSubmit)="save()">
            <div class="field"><label>Nome</label><input formControlName="name" /></div>
            <div class="field"><label>E-mail</label><input type="email" formControlName="email" /></div>
            <div class="field"><label>Senha</label><input type="password" formControlName="password" /></div>
            <div class="field">
              <label>Perfil</label>
              <select formControlName="role">
                <option *ngFor="let r of roles" [value]="r">{{ r }}</option>
              </select>
            </div>
            <div class="actions">
              <button type="button" class="btn-secondary" (click)="closeForm()">Cancelar</button>
              <button type="submit" class="btn-primary" [disabled]="form.invalid">Salvar</button>
            </div>
          </form>
        </div>
      </div>

      <table *ngIf="users.length">
        <thead><tr><th>Nome</th><th>E-mail</th><th>Perfil</th><th>Ações</th></tr></thead>
        <tbody>
          <tr *ngFor="let u of users">
            <td>{{ u.name }}</td>
            <td>{{ u.email }}</td>
            <td><span class="badge">{{ u.role }}</span></td>
            <td class="actions-cell">
              <button class="btn-edit" (click)="edit(u)">Editar</button>
              <button class="btn-delete" (click)="remove(u.id!)">Excluir</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p *ngIf="!users.length && !loading" class="empty">Nenhum usuário cadastrado.</p>
    </div>
  `,
  styleUrls: ['../shared-crud.scss']
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  form!: FormGroup;
  showForm = false;
  editing: User | null = null;
  loading = false;
  roles: UserRole[] = ['admin', 'editor', 'professor', 'student', 'viewer'];

  constructor(private fb: FormBuilder, private service: UserService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.service.getAll().subscribe({ next: d => { this.users = d; this.loading = false; }, error: () => this.loading = false });
  }

  openForm(user?: User): void {
    this.editing = user || null;
    this.form = this.fb.group({
      name: [user?.name || '', Validators.required],
      email: [user?.email || '', [Validators.required, Validators.email]],
      password: [user?.password || '', this.editing ? [] : [Validators.required]],
      role: [user?.role || 'viewer', Validators.required]
    });
    this.showForm = true;
  }

  edit(user: User): void { this.openForm(user); }
  closeForm(): void { this.showForm = false; this.editing = null; }

  save(): void {
    const data = this.form.value as User;
    const op = this.editing
      ? this.service.update(this.editing.id!, data)
      : this.service.create(data);
    op.subscribe({ next: () => { this.load(); this.closeForm(); } });
  }

  remove(id: number): void {
    if (confirm('Confirmar exclusão?')) this.service.delete(id).subscribe(() => this.load());
  }
}
