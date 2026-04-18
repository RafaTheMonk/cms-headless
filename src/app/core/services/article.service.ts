import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Article } from '../models/article.model';

@Injectable({ providedIn: 'root' })
export class ArticleService {
  constructor(private api: ApiService) {}

  getAll(): Observable<Article[]> { return this.api.get<Article[]>('/articles'); }
  getById(id: number): Observable<Article> { return this.api.get<Article>(`/articles/${id}`); }
  create(article: Article): Observable<Article> { return this.api.post<Article>('/articles', article); }
  update(id: number, article: Article): Observable<Article> { return this.api.put<Article>(`/articles/${id}`, article); }
  delete(id: number): Observable<void> { return this.api.delete<void>(`/articles/${id}`); }
}
