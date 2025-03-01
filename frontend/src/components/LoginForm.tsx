import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';

interface LoginRequest {
    username: string;
    password: string;
}

interface JwtResponse {
    token: string;
    type: string;
    username: string;
}

const LoginForm: React.FC = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleLogin = async (event: React.FormEvent) => {
        event.preventDefault();
        
        const loginData: LoginRequest = { username, password };

        try {
            const response = await axios.post<JwtResponse>('/api/perform_login', loginData);

            if (response.status === 200) {
                const { token, username } = response.data;
                localStorage.setItem('token', token);
                localStorage.setItem('username', username);
                navigate('/myleagues');
            } else {
                setError('Invalid credentials');
            }
        } catch (error) {
            setError('An error occurred during login');
        }
    };

    return (
        <div className="container d-flex justify-content-center align-items-center">
            <div className="card p-4">
                <h2 className="text-center mb-4">Login</h2>
                {error && <p className="text-danger text-center">{error}</p>}
                <form onSubmit={handleLogin}>
                    <div className="mb-3">
                        <label htmlFor="username" className="form-label">Username:</label>
                        <input
                            type="text"
                            id="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            className="form-control"
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="password" className="form-label">Password:</label>
                        <input
                            type="password"
                            id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="form-control"
                            required
                        />
                    </div>
                    <button type="submit" className="btn btn-primary">Login</button>
                </form>
                <p className="mt-3 text-center">Don't have an account? <Link to="/registration" className="text-primary">Register here</Link></p>
            </div>
        </div>
    );
};

export default LoginForm;
