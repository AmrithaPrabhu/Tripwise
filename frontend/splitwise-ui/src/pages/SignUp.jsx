import React, { useState } from 'react'
import {useNavigate} from 'react-router-dom';
import AuthLogo from '../components/AuthLogo';
import "./Signup.css";
import axios from "../utils/axiosInstance"; 
import { setToken } from "../utils/auth";
const SignUp = () => {
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: ""
  });
  const navigate = useNavigate();
  const goToLogin = () => {
    navigate('/login')
  }
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [error, setError] = useState("");
  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value
    });
  };
  const handleSubmit = async (e) => {
        e.preventDefault()
        if (form.password !== form.confirmPassword) {
            setError("Passwords do not match");
            return;
        }
        try {
            const res = await axios.post("/auth/register", {
            name: form.name,
            email: form.email,
            password: form.password,
        });

            setToken(res.data.res);
            navigate("/");
        } catch (err) {
            console.log(err)
            setError(err.response.data.message);
        }
  }
  return (
    <div>
        <div className='container'>
            <AuthLogo show={showConfirmPassword || showPassword} />
            
            <div className='form-class'>
                <p className='form-title'>Sign Up</p>
                <form className='form-contents'>
                    {error.length > 0 && ( <p className="error-text">{error}</p>)}
                    <input
                        type="text"
                        name="name"
                        placeholder="Full Name"
                        value={form.name}
                        onChange={handleChange}
                        className='input-box'
                    />

                    <input
                        type="email"
                        name="email"
                        placeholder="Email"
                        value={form.email}
                        onChange={handleChange}
                        className='input-box'
                    />

                    <div className="password-field">
                        <input
                            type={showPassword ? "text" : "password"}
                            name="password"
                            placeholder="Password"
                            value={form.password}
                            onChange={handleChange}
                            className='input-box'
                        />
                        <button
                            type="button"
                            className="show-btn"
                            onClick={() => setShowPassword(prev => !prev)}
                        >
                            {showPassword ? "Hide" : "Show"}
                        </button>
                    </div>
                    <div className="password-field">
                        <input
                            type={showConfirmPassword ? "text" : "password"}
                            name="confirmPassword"
                            placeholder="Confirm Password"
                            value={form.confirmPassword}
                            onChange={handleChange}
                            className='input-box'
                        />
                        <button
                            type="button"
                            className="show-btn"
                            onClick={() => setShowConfirmPassword(prev => !prev)}
                        >
                            {showConfirmPassword ? "Hide" : "Show"}
                        </button>
                    </div>
                    <div>
                        Have an account already ? 
                        <button type='button' onClick={goToLogin} className='log-btn'>Log In instead</button>
                    </div>
                    <br></br>
                    <button className="signup-btn" onClick={handleSubmit}>Create account</button>
                </form>
            </div>
        </div>
    </div>
  )
}

export default SignUp
