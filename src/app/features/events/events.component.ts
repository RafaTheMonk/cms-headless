import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { EventService } from '../../core/services/event.service';
import { Event, EventModality, ContentStatus } from '../../core/models/event.model';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page">
      <div class="header">
        <h2>Eventos</h2>
        <button class="btn-primary" (click)="openForm()">+ Novo Evento</button>
      </div>

      <div class="modal-overlay" *ngIf="showForm" (click)="closeForm()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>{{ editing ? 'Editar' : 'Novo' }} Evento</h3>
          <form [formGroup]="form" (ngSubmit)="save()">
            <div class="field"><label>Título</label><input formControlName="title" /></div>
            <div class="field"><label>Descrição</label><textarea formControlName="description"></textarea></div>
            <div class="field">
              <label>Modalidade</label>
              <select formControlName="modality">
                <option *ngFor="let m of modalities" [value]="m">{{ m }}</option>
              </select>
            </div>
            <div class="field"><label>Local</label><input formControlName="location" /></div>
            <div class="field"><label>Data do Evento</label><input type="datetime-local" formControlName="eventDate" /></div>
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
        <thead><tr><th>Título</th><th>Modalidade</th><th>Data</th><th>Status</th><th>Ações</th></tr></thead>
        <tbody>
          <tr *ngFor="let e of items">
            <td>{{ e.title }}</td>
            <td><span class="badge">{{ e.modality }}</span></td>
            <td>{{ e.eventDate | date:'dd/MM/yyyy HH:mm' }}</td>
            <td><span class="badge badge-{{e.status}}">{{ e.status }}</span></td>
            <td class="actions-cell">
              <button class="btn-edit" (click)="edit(e)">Editar</button>
              <button class="btn-delete" (click)="remove(e.id!)">Excluir</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p *ngIf="!items.length && !loading" class="empty">Nenhum evento cadastrado.</p>
    </div>
  `,
  styleUrls: ['../shared-crud.scss']
})
export class EventsComponent implements OnInit {
  items: Event[] = [];
  form!: FormGroup;
  showForm = false;
  editing: Event | null = null;
  loading = false;
  modalities: EventModality[] = ['presencial', 'online', 'hibrido'];
  statuses: ContentStatus[] = ['draft', 'in_review', 'published', 'archived'];

  constructor(private fb: FormBuilder, private service: EventService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.service.getAll().subscribe({ next: d => { this.items = d; this.loading = false; }, error: () => this.loading = false });
  }

  openForm(item?: Event): void {
    this.editing = item || null;
    this.form = this.fb.group({
      title: [item?.title || '', Validators.required],
      description: [item?.description || '', Validators.required],
      modality: [item?.modality || 'presencial', Validators.required],
      location: [item?.location || ''],
      eventDate: [item?.eventDate || '', Validators.required],
      status: [item?.status || 'draft', Validators.required]
    });
    this.showForm = true;
  }

  edit(item: Event): void { this.openForm(item); }
  closeForm(): void { this.showForm = false; this.editing = null; }

  save(): void {
    const data = this.form.value as Event;
    const op = this.editing ? this.service.update(this.editing.id!, data) : this.service.create(data);
    op.subscribe({ next: () => { this.load(); this.closeForm(); } });
  }

  remove(id: number): void {
    if (confirm('Confirmar exclusão?')) this.service.delete(id).subscribe(() => this.load());
  }
}
