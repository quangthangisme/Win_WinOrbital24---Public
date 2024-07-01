import React, { useState, ChangeEvent, FormEvent } from 'react';
import axios, { AxiosError } from 'axios';
import { useNavigate, Link } from 'react-router-dom';

interface UserDto {
    username: string;
    email: string;
    password: string;
    matchingPassword: string;
}

const RegistrationForm: React.FC = () => {
    const [formData, setFormData] = useState<UserDto>({
        username: '',
        email: '',
        password: '',
        matchingPassword: ''
    });
    const [error, setError] = useState<string | null>(null);
    const [validationError, setValidationError] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleChange = (e: ChangeEvent<HTMLInputElement>): void => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
    };

    const validateForm = (): boolean => {
        const { username, email, password, matchingPassword } = formData;


        if (!username || !email || !password || !matchingPassword) {
            setValidationError('All fields are required.');
            return false;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            setValidationError('Invalid email format.');
            return false;
        }

        if (password !== matchingPassword) {
            setValidationError('Passwords do not match.');
            return false;
        }

        setValidationError(null);
        return true;
    };

    const handleSubmit = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            const userDto: UserDto = { ...formData };
            const response = await axios.post('/api/registration', userDto);
            if (response.status === 200) {
                navigate('/login');
            }
        } catch (error) {
            if (axios.isAxiosError(error)) {
                const axiosError = error as AxiosError;
                if (axiosError.response && axiosError.response.status === 409) {
                    setError("An account for that username/email already exists.");
                } else if (axiosError.response && axiosError.response.status === 400) {
                    setError("An unexpected error occurred.");
                } else {
                    console.error('An unexpected error occurred:', axiosError.message);
                    setError('An unexpected error occurred. Please try again later.');
                }
            }
        }
    };

    return (
        <div className="container">
            <div className="row justify-content-center mt-5">
                <div className="col-md-4">
                    <div className="card p-4">
                        <h2 className="text-center mb-4">Registration</h2>
                        <form onSubmit={handleSubmit}>
                            {error && <p className="text-danger text-center">{error}</p>}
                            {validationError && <p className="text-danger text-center">{validationError}</p>}
                            <div className="mb-3">
                                <label htmlFor="username" className="form-label">Username:</label>
                                <input
                                    type="text"
                                    id="username"
                                    name="username"
                                    value={formData.username}
                                    onChange={handleChange}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <div className="mb-3">
                                <label htmlFor="email" className="form-label">Email:</label>
                                <input
                                    type="email"
                                    id="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <div className="mb-3">
                                <label htmlFor="password" className="form-label">Password:</label>
                                <input
                                    type="password"
                                    id="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <div className="mb-3">
                                <label htmlFor="matchingPassword" className="form-label">Confirm Password:</label>
                                <input
                                    type="password"
                                    id="matchingPassword"
                                    name="matchingPassword"
                                    value={formData.matchingPassword}
                                    onChange={handleChange}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <button type="submit" className="btn btn-primary">Submit</button>
                        </form>
                        <p className="mt-3 text-center">Already have an account? <Link to="/login">Login here</Link></p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RegistrationForm;