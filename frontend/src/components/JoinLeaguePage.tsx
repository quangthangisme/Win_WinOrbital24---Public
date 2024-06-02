import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios, { AxiosError } from 'axios';

const JoinLeaguePage: React.FC = () => {
    const [leagueCode, setLeagueCode] = useState('');
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        const checkAuthenticated = async () => {
            try {
                const response = await axios.get('/api/authenticated');
                if (response.status !== 200) {
                    navigate('/login');
                }
            } catch (error) {
                navigate('/login');
            }
        };

        checkAuthenticated();
    }, [navigate]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {

            if (!leagueCode.trim()) {
                setError('League code is required');
                return;
            }

            const params = new URLSearchParams();
            params.append('code', leagueCode);

            const response = await axios.post('/api/league/join', params);
            if (response.status === 200) {
                navigate('/myleagues');
            }
        } catch (error) {
            if (axios.isAxiosError(error)) {
                const axiosError = error as AxiosError;
                if (axiosError.response?.status === 401) {
                    navigate('/login');
                } else {
                    const errorMessage = axiosError.response?.data?.toString() || 'Failed to join league';
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
                        <h2 className="text-center mb-4">Join League</h2>
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label htmlFor="leagueCode" className="form-label">League Code:</label>
                                <input
                                    type="text"
                                    id="leagueCode"
                                    value={leagueCode}
                                    onChange={(e) => setLeagueCode(e.target.value)}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <button type="submit" className="btn btn-primary">Join</button>
                        </form>
                        {error && <p className="text-danger mt-3">{error}</p>}
                        <Link to="/myleagues" className="mt-3 d-block text-center">Back to My Leagues</Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default JoinLeaguePage;
