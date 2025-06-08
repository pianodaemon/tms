export interface SSOClient<T> {
  signIn(username: string, password: string): Promise<T>;
  signOut(): Promise<void>;
  getCurrentUser(): Promise<T | null>;
  isAuthenticated(): Promise<boolean>;
}
