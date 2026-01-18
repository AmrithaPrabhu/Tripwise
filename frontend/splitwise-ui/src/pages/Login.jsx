import { useState } from "react";
import {replace, useNavigate} from 'react-router-dom';
import axios from "../utils/axiosInstance"; 
import AuthLogo from "../components/AuthLogo";
import "./Login.css";
import { setToken } from "../utils/auth";

const Login = () => {
  const [form, setForm] = useState({
      email: "",
      password: "",
    });
    const navigate = useNavigate();
  const goToSignUp = () => {
    navigate('/signup')
  }
    const [error, setError] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const handleChange = (e) => {
      setForm({
        ...form,
        [e.target.name]: e.target.value
      });
    };

    const handleSubmit = async (e) => {
      e.preventDefault();
      try {
            const res = await axios.post("/auth/login", {
            email: form.email,
            password: form.password,
        });
            console.log(res)
            setToken(res.data.res);
            navigate("/");
        } catch (err) {
            console.log(err);

            const message = err.response?.data?.message ||
              "Login failed. Please try again.";

            setError(message);
        }
    };
  return (
    <div>
        <div className='container'>
            <AuthLogo show={showPassword} />
            
            <div className='form-class'>
                <p className='form-title'>Login</p>
                <form className='form-contents'>
                  {error.length > 0 && ( <p className="error-text">{error}</p>)}
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
                    <div>
                        Don't have an account ? 
                        <button type="button" onClick={goToSignUp} className='log-btn'>Sign Up instead</button>
                    </div>
                    <br></br>
                    <button className="signup-btn" onClick={handleSubmit}>Login</button>
                </form>
            </div>
        </div>
    </div>
  );
};

export default Login;
