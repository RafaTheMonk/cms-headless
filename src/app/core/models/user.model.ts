export type UserRole = 'admin' | 'editor' | 'professor' | 'student' | 'viewer';

export interface User {
  id?: number;
  name: string;
  email: string;
  password?: string;
  role: UserRole;
  createdAt?: string;
}
