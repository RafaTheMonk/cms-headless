import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Project } from '../models/project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  constructor(private api: ApiService) {}

  getAll(): Observable<Project[]> { return this.api.get<Project[]>('/projects'); }
  getById(id: number): Observable<Project> { return this.api.get<Project>(`/projects/${id}`); }
  create(project: Project): Observable<Project> { return this.api.post<Project>('/projects', project); }
  update(id: number, project: Project): Observable<Project> { return this.api.put<Project>(`/projects/${id}`, project); }
  delete(id: number): Observable<void> { return this.api.delete<void>(`/projects/${id}`); }
}
