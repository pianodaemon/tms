import {
  AuthenticationDetails,
  CognitoUser,
  CognitoUserPool,
} from 'amazon-cognito-identity-js';
import type { SSOClient } from './SSOClient';

export interface AuthUser {
  id: string;
  email: string;
  token: string;
}

const poolData = {
  UserPoolId: import.meta.env.VITE_COGNITO_USER_POOL_ID,
  ClientId: import.meta.env.VITE_COGNITO_CLIENT_ID,
};

const userPool = new CognitoUserPool(poolData);

export class AWSCognitoSSOClient implements SSOClient<AuthUser> {
  async signIn(username: string, password: string): Promise<AuthUser> {
    const user = new CognitoUser({ Username: username, Pool: userPool });
    const authDetails = new AuthenticationDetails({
      Username: username,
      Password: password,
    });

    return new Promise((resolve, reject) => {
      user.authenticateUser(authDetails, {
        onSuccess: (result) => {
          const token = result.getIdToken().getJwtToken();
          resolve({
            id: username,
            email: username, // Adjust if needed
            token,
          });
        },
        onFailure: (err) => {
          reject(err);
        },
      });
    });
  }

  async signOut(): Promise<void> {
    const user = userPool.getCurrentUser();
    user?.signOut();
  }

  async getCurrentUser(): Promise<AuthUser | null> {
    const user = userPool.getCurrentUser();

    return new Promise((resolve) => {
      if (!user) return resolve(null);

      user.getSession((err: any, session: any) => {
        if (err || !session.isValid()) {
          resolve(null);
        } else {
          resolve({
            id: user.getUsername(),
            email: user.getUsername(), // Adjust if needed
            token: session.getIdToken().getJwtToken(),
          });
        }
      });
    });
  }

  async isAuthenticated(): Promise<boolean> {
    const user = await this.getCurrentUser();
    return !!user;
  }
}
