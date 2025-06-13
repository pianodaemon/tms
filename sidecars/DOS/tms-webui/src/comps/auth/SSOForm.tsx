import React, { useState } from 'react';
import type { SSOClient } from '../../ipc/auth/SSOClient';
import type { AuthUser } from '../../ipc/auth/AuthUser';

interface SSOFormProps {
  ssoClient: SSOClient<AuthUser>;
}

const SSOForm: React.FC<SSOFormProps> = ({ ssoClient }) => {

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [user, setUser] = useState<AuthUser | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const authUser = await ssoClient.signIn(username, password);
      setUser(authUser);
    } catch (err: any) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    await ssoClient.signOut();
    setUser(null);
  };

  if (user) {
    return (
      <div>
        <p>Welcome, {user.email}</p>
        <button onClick={handleLogout}>Sign out</button>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit}>
      <h2>Login</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <div>
        <label>
          Username:
          <input
            type="email"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            autoComplete="username"
          />
        </label>
      </div>
      <div>
        <label>
          Password:
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="current-password"
          />
        </label>
      </div>
      <button type="submit" disabled={loading}>
        {loading ? 'Signing in…' : 'Sign in'}
      </button>
    </form>
  );
};

export default SSOForm;