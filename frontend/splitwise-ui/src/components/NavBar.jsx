import React, { useEffect, useState } from 'react'
import "./NavBar.css";
import { clearToken } from '../utils/auth';
import {useNavigate} from 'react-router-dom';
import axiosInstance from '../utils/axiosInstance';

const NavBar = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const navigate = useNavigate();
  useEffect(()=> {
    axiosInstance
      .get("/auth/me")
      .then(res => setUser(res.data))
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  })
  if (loading){
     return (
      <header className='fixed left-0 right-0 z-50 py-4'>
        <nav className='navbar'>
          <p>Tripwise</p>
          <div className='logout-area'>
            <p>Loading...</p>
          </div>
        </nav>
      </header>
    );
  }
  const handleLogout = (e) => {
    e.preventDefault()
    clearToken()
    navigate("/login")
  }
  return (
    <header className='fixed left-0 right-0 z-50 py-4'>
    <nav className='navbar'>
      <p>Tripwise</p>
      <div className='logout-area'>
        <p>{user?.name}</p>
        <button type='submit' onClick={handleLogout} className='logout-btn'>Logout</button>
      </div>
    </nav>
    </header>
  )
}

export default NavBar
