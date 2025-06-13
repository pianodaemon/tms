import SSOForm from '../comps/auth/SSOForm';
import { AmplifySSOClient } from '../ipc/auth/AmplifySSOClient';

const ssoClient = new AmplifySSOClient();

function Login() {

  return (
    <div style={{ padding: '2rem' }}>
      <SSOForm ssoClient={ssoClient} />
    </div>
  );

}

export default Login