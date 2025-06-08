import { Routes, Route, Navigate } from 'react-router-dom';
import Login from '../pages/Login';
import Home from '../pages/Home';

function HalfBakeRouter() {

  const homePath: string = "/home";

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path={homePath} element={<Home />} />

      <Route path="/*" element={<Navigate to={homePath} replace />} />
    </Routes>
  );
}

export default HalfBakeRouter;
