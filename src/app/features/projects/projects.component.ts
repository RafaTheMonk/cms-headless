import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProjectService } from '../../core/services/project.service';
import { Project } from '../../core/models/project.model';
import { ContentStatus } from '../../core/models/news.model';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page">
      <div class="header">
        <h2>Projetos</h2>
        <button class="btn-primary" (click)="openForm()">+ Novo Projeto</button>
      </div>

      <div class="modal-overlay" *ngIf="showForm" (click)="closeForm()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>{{ editing ? 'Editar' : 'Novo' }} Projeto</h3>
          <form [formGroup]="form" (ngSubmit)="save()">
            <div class="field"><label>Título</label><input formControlName="title" /></div>
            <div class="field"><label>Descrição</label><textarea formControlName="description"></textarea></div>
            <div class="field"><label>Início</label><input type="date" formControlName="startDate" /></div>
            <div class="field"><label>Fim</label><input type="date" formControlName="endDate" /></div>
            <div class="field">
              <label>Status</label>
              <select formControlName="status">
                <option *ngFor="let s of statuses" [value]="s">{{ s }}</option>
              </select>
            </div>
            <div class="actions">
              <button type="button" class="btn-secondary" (click)="closeForm()">Cancelar</button>
              <button type="submit" class="btn-primary" [disabled]="form.invalid">Salvar</button>
            </div>
          </form>
        </div>
      </div>

      <table *ngIf="items.length">
        <thead><tr><th>Título</th><th>Status</th><th>Início</th><th>Fim</th><th>Ações</th></tr></thead>
        <tbody>
          <tr *ngFor="let p of items">
            <td>{{ p.title }}</td>
            <td><span class="badge badge-{{p.status}}">{{ p.status }}</span></td>
            <td>{{ p.startDate | date:'dd/MM/yyyy' }}</td>
            <td>{{ p.endDate | date:'dd/MM/yyyy' }}</td>
            <td class="actions-cell">
              <button class="btn-edit" (click)="edit(p)">Editar</button>
              <button class="btn-delete" (click)="remove(p.id!)">Excluir</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p *ngIf="!items.length && !loading" class="empty">Nenhum projeto cadastrado.</p>
    </div>
  `,
  styleUrls: ['../shared-crud.scss']
})
export class ProjectsComponent implements OnInit {
  items: Project[] = [];
  form!: FormGroup;
  showForm = false;
  editing: Project | null = null;
  loading = false;
  statuses: ContentStatus[] = ['draft', 'in_review', 'published', 'archived'];

  constructor(private fb: FormBuilder, private service: ProjectService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.service.getAll().subscribe({ next: d => { this.items = d; this.loading = false; }, error: () => this.loading = false });
  }

  openForm(item?: Project): void {
    this.editing = item || null;
    this.form = this.fb.group({
      title: [item?.title || '', Validators.required],
      description: [item?.description || '', Validators.required],
      startDate: [item?.startDate || ''],
      endDate: [item?.endDate || ''],
      status: [item?.status || 'draft', Validators.required]
    });
    this.showForm = true;
  }

  edit(item: Project): void { this.openForm(item); }
  closeForm(): void { this.showForm = false; this.editing = null; }

  save(): void {
    const data = this.form.value as Project;
    const op = this.editing ? this.service.update(this.editing.id!, data) : this.service.create(data);
    op.subscribe({ next: () => { this.load(); this.closeForm(); } });
  }

  remove(id: number): void {
    if (confirm('Confirmar exclusão?')) this.service.delete(id).subscribe(() => this.load());
  }
}
