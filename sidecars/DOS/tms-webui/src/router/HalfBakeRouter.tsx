import { Routes, Route } from 'react-router-dom';
import Login from '../pages/Login';
import Home from '../pages/Home';

function HalfBakeRouter() {

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/home" element={<Home />} />
     
    </Routes>
  );
}

export default HalfBakeRouter;
