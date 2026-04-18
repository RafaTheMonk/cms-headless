import { Injectable, signal } from '@angular/core';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _currentUser = signal<User | null>(null);
  readonly currentUser = this._currentUser.asReadonly();

  login(user: User): void {
    this._currentUser.set(user);
    localStorage.setItem('cms_user', JSON.stringify(user));
  }

  logout(): void {
    this._currentUser.set(null);
    localStorage.removeItem('cms_user');
  }

  loadFromStorage(): void {
    const stored = localStorage.getItem('cms_user');
    if (stored) this._currentUser.set(JSON.parse(stored));
  }

  isLoggedIn(): boolean {
    return this._currentUser() !== null;
  }

  hasRole(...roles: string[]): boolean {
    const user = this._currentUser();
    return user ? roles.includes(user.role) : false;
  }
}
