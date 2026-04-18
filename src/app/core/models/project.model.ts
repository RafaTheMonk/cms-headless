import { ContentStatus } from './news.model';

export interface Project {
  id?: number;
  title: string;
  description: string;
  responsibleId?: number;
  status: ContentStatus;
  startDate?: string;
  endDate?: string;
  createdAt?: string;
}
