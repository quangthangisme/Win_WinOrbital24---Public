import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios, { AxiosError } from 'axios';

const CreateLeaguePage: React.FC = () => {
    const [leagueName, setLeagueName] = useState('');
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {

            if (!leagueName.trim()) {
                setError('League name is required');
                return;
            }

            const params = new URLSearchParams();
            params.append('name', leagueName);

            const response = await axios.post('/api/league/create', params);
            if (response.status === 200) {
                navigate('/myleagues');
            }
        } catch (error) {
            if (axios.isAxiosError(error)) {
                const axiosError = error as AxiosError;
                if (axiosError.response?.status === 401) {
                    navigate('/login');
                } else if (axiosError.response?.status === 400) {
                    setError('League name is required');
                } else {
                    const errorMessage = axiosError.response?.data?.toString() || 'Failed to create league';
                    setError(errorMessage);
                }
            } else {
                setError('An unexpected error occurred');
            }
        }
    };

    return (
        <div className="container">
            <div className="row justify-content-center mt-5">
                <div className="col-md-6">
                    <div className="card p-4">
                        <h2 className="text-center mb-4">Create League</h2>
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label htmlFor="leagueName" className="form-label">League Name:</label>
                                <input
                                    type="text"
                                    id="leagueName"
                                    value={leagueName}
                                    onChange={(e) => setLeagueName(e.target.value)}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <button type="submit" className="btn btn-primary">Create</button>
                        </form>
                        {error && <p className="text-danger mt-3">{error}</p>}
                        <Link to="/myleagues" className="mt-3 d-block text-center">Back to My Leagues</Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CreateLeaguePage;
