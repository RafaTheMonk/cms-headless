import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ArticleService } from '../../core/services/article.service';
import { Article } from '../../core/models/article.model';
import { ContentStatus } from '../../core/models/news.model';

@Component({
  selector: 'app-articles',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page">
      <div class="header">
        <h2>Artigos</h2>
        <button class="btn-primary" (click)="openForm()">+ Novo Artigo</button>
      </div>

      <div class="modal-overlay" *ngIf="showForm" (click)="closeForm()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>{{ editing ? 'Editar' : 'Novo' }} Artigo</h3>
          <form [formGroup]="form" (ngSubmit)="save()">
            <div class="field"><label>Título</label><input formControlName="title" /></div>
            <div class="field"><label>Conteúdo</label><textarea formControlName="content"></textarea></div>
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
        <thead><tr><th>Título</th><th>Status</th><th>Criado em</th><th>Ações</th></tr></thead>
        <tbody>
          <tr *ngFor="let a of items">
            <td>{{ a.title }}</td>
            <td><span class="badge badge-{{a.status}}">{{ a.status }}</span></td>
            <td>{{ a.createdAt | date:'dd/MM/yyyy' }}</td>
            <td class="actions-cell">
              <button class="btn-edit" (click)="edit(a)">Editar</button>
              <button class="btn-delete" (click)="remove(a.id!)">Excluir</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p *ngIf="!items.length && !loading" class="empty">Nenhum artigo cadastrado.</p>
    </div>
  `,
  styleUrls: ['../shared-crud.scss']
})
export class ArticlesComponent implements OnInit {
  items: Article[] = [];
  form!: FormGroup;
  showForm = false;
  editing: Article | null = null;
  loading = false;
  statuses: ContentStatus[] = ['draft', 'in_review', 'published', 'archived'];

  constructor(private fb: FormBuilder, private service: ArticleService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.service.getAll().subscribe({ next: d => { this.items = d; this.loading = false; }, error: () => this.loading = false });
  }

  openForm(item?: Article): void {
    this.editing = item || null;
    this.form = this.fb.group({
      title: [item?.title || '', Validators.required],
      content: [item?.content || '', Validators.required],
      status: [item?.status || 'draft', Validators.required]
    });
    this.showForm = true;
  }

  edit(item: Article): void { this.openForm(item); }
  closeForm(): void { this.showForm = false; this.editing = null; }

  save(): void {
    const data = this.form.value as Article;
    const op = this.editing ? this.service.update(this.editing.id!, data) : this.service.create(data);
    op.subscribe({ next: () => { this.load(); this.closeForm(); } });
  }

  remove(id: number): void {
    if (confirm('Confirmar exclusão?')) this.service.delete(id).subscribe(() => this.load());
  }
}
