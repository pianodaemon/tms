import ehandle from '../../icskit/ehandle';
import type { SSOClient } from './SSOClient';
import type { AuthUser } from './AuthUser';
import {
  signIn,
  getCurrentUser,
  fetchAuthSession,
  signOut as amplifySignOut,
  type SignInOutput,
} from 'aws-amplify/auth';

export class AmplifySSOClient implements SSOClient<AuthUser> {
  async signIn(username: string, password: string): Promise<AuthUser> {
    const [error, _] = await ehandle.catchError<SignInOutput>(signIn({ username, password }));
    if (error) {
      console.error("SSO client can not sign in : ", error.message);
    }
    //await signIn({ username, password });
    return this.buildAuthUser();
  }

  async signOut(): Promise<void> {
    await amplifySignOut();
  }

  async getCurrentUser(): Promise<AuthUser | null> {
    try {
      return await this.buildAuthUser();
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

  private async buildAuthUser(): Promise<AuthUser> {
    const user = await getCurrentUser();
    const session = await fetchAuthSession();

    return {
      id: user.userId,
      email: user.signInDetails?.loginId ?? user.userId,
      token: session.tokens?.idToken?.toString() ?? '',
    };
  }
}
