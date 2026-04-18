import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { News } from '../models/news.model';

@Injectable({ providedIn: 'root' })
export class NewsService {
  constructor(private api: ApiService) {}

  getAll(): Observable<News[]> { return this.api.get<News[]>('/news'); }
  getById(id: number): Observable<News> { return this.api.get<News>(`/news/${id}`); }
  create(news: News): Observable<News> { return this.api.post<News>('/news', news); }
  update(id: number, news: News): Observable<News> { return this.api.put<News>(`/news/${id}`, news); }
  delete(id: number): Observable<void> { return this.api.delete<void>(`/news/${id}`); }
}
