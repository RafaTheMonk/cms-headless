import { ContentStatus } from './news.model';

export interface Article {
  id?: number;
  title: string;
  content: string;
  authorId?: number;
  status: ContentStatus;
  publishedAt?: string;
  createdAt?: string;
}
