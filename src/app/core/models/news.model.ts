export type ContentStatus = 'draft' | 'in_review' | 'published' | 'archived';

export interface News {
  id?: number;
  title: string;
  content: string;
  authorId?: number;
  status: ContentStatus;
  publishedAt?: string;
  createdAt?: string;
}
