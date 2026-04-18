export type EventModality = 'presencial' | 'online' | 'hibrido';
export type ContentStatus = 'draft' | 'in_review' | 'published' | 'archived';

export interface Event {
  id?: number;
  title: string;
  description: string;
  modality: EventModality;
  location?: string;
  eventDate: string;
  status: ContentStatus;
  createdAt?: string;
}
