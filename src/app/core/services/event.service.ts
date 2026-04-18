import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Event } from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class EventService {
  constructor(private api: ApiService) {}

  getAll(): Observable<Event[]> { return this.api.get<Event[]>('/events'); }
  getById(id: number): Observable<Event> { return this.api.get<Event>(`/events/${id}`); }
  create(event: Event): Observable<Event> { return this.api.post<Event>('/events', event); }
  update(id: number, event: Event): Observable<Event> { return this.api.put<Event>(`/events/${id}`, event); }
  delete(id: number): Observable<void> { return this.api.delete<void>(`/events/${id}`); }
}
