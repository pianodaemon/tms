import type { SSOClient } from './SSOClient';
import type { AuthUser } from './AuthUser';
import { signIn, getCurrentUser, fetchAuthSession, signOut } from 'aws-amplify/auth';
//import { AuthUser, SSOClient } from './SSOClient'; // assuming these are defined

export class AmplifySSOClient implements SSOClient<AuthUser> {

  async signIn(username: string, password: string): Promise<AuthUser> {

    await signIn({ username, password });

    const user = await getCurrentUser();
    const session = await fetchAuthSession();

    return {
      id: user.userId,
      email: user.signInDetails?.loginId ?? user.userId,
      token: session.tokens?.idToken?.toString() ?? '',
    };
  }

  async signOut(): Promise<void> {
    await signOut();
  }

  async getCurrentUser(): Promise<AuthUser | null> {
    try {
      const user = await getCurrentUser();
      const session = await fetchAuthSession();

      return {
        id: user.username,
        email: user.signInDetails?.loginId ?? user.username,
        token: session.tokens?.idToken?.toString() ?? '',
      };
    } catch {
      return null;
    }
  }

  async isAuthenticated(): Promise<boolean> {
    try {
      const session = await fetchAuthSession();
      return !!session.tokens?.idToken;
    } catch {
      return false;
    }
  }
}