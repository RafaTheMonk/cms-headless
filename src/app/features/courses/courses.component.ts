import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CourseService } from '../../core/services/course.service';
import { Course } from '../../core/models/course.model';

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page">
      <div class="header">
        <h2>Cursos</h2>
        <button class="btn-primary" (click)="openForm()">+ Novo Curso</button>
      </div>

      <div class="modal-overlay" *ngIf="showForm" (click)="closeForm()">
        <div class="modal" (click)="$event.stopPropagation()">
          <h3>{{ editing ? 'Editar' : 'Novo' }} Curso</h3>
          <form [formGroup]="form" (ngSubmit)="save()">
            <div class="field"><label>Nome</label><input formControlName="name" /></div>
            <div class="field"><label>Descrição</label><textarea formControlName="description"></textarea></div>
            <div class="actions">
              <button type="button" class="btn-secondary" (click)="closeForm()">Cancelar</button>
              <button type="submit" class="btn-primary" [disabled]="form.invalid">Salvar</button>
            </div>
          </form>
        </div>
      </div>

      <table *ngIf="courses.length">
        <thead><tr><th>Nome</th><th>Descrição</th><th>Ações</th></tr></thead>
        <tbody>
          <tr *ngFor="let c of courses">
            <td>{{ c.name }}</td>
            <td>{{ c.description }}</td>
            <td class="actions-cell">
              <button class="btn-edit" (click)="edit(c)">Editar</button>
              <button class="btn-delete" (click)="remove(c.id!)">Excluir</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p *ngIf="!courses.length && !loading" class="empty">Nenhum curso cadastrado.</p>
    </div>
  `,
  styleUrls: ['../shared-crud.scss']
})
export class CoursesComponent implements OnInit {
  courses: Course[] = [];
  form!: FormGroup;
  showForm = false;
  editing: Course | null = null;
  loading = false;

  constructor(private fb: FormBuilder, private service: CourseService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.service.getAll().subscribe({ next: d => { this.courses = d; this.loading = false; }, error: () => this.loading = false });
  }

  openForm(course?: Course): void {
    this.editing = course || null;
    this.form = this.fb.group({
      name: [course?.name || '', Validators.required],
      description: [course?.description || '', Validators.required]
    });
    this.showForm = true;
  }

  edit(course: Course): void { this.openForm(course); }
  closeForm(): void { this.showForm = false; this.editing = null; }

  save(): void {
    const data = this.form.value as Course;
    const op = this.editing ? this.service.update(this.editing.id!, data) : this.service.create(data);
    op.subscribe({ next: () => { this.load(); this.closeForm(); } });
  }

  remove(id: number): void {
    if (confirm('Confirmar exclusão?')) this.service.delete(id).subscribe(() => this.load());
  }
}
