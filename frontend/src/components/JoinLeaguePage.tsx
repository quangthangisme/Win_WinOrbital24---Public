import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios, { AxiosError } from 'axios';
import { Spinner } from 'react-bootstrap';

const JoinLeaguePage: React.FC = () => {
    const [leagueCode, setLeagueCode] = useState('');
    const [teamName, setTeamName] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const navigate = useNavigate();

    useEffect(() => {
        const checkAuthenticated = async () => {
            try {
                const token = localStorage.getItem('token');
                if (!token) {
                    navigate('/login');
                }
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            } catch (error: any) {
                console.error('Error checking authentication:', error);
                navigate('/login');
            }
        };

        checkAuthenticated();
    }, [navigate]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            if (!leagueCode.trim()) {
                setError('League code is required');
                setLoading(false);
                return;
            }
            
            if (!teamName.trim()) {
                setError('Team name is required');
                setLoading(false);
                return;
            }

            const payload = {
                code: leagueCode,
                teamName: teamName
            };

            const response = await axios.post('/api/league/join', payload);

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
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <div className="row justify-content-center m-5">
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
                            <div className="mb-3">
                                <label htmlFor="teamName" className="form-label">Team Name:</label>
                                <input
                                    type="text"
                                    id="teamName"
                                    value={teamName}
                                    onChange={(e) => setTeamName(e.target.value)}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <button type="submit" className="btn btn-primary">Join</button>
                        </form>
                        {loading && (
                            <div className="text-center mt-3">
                                <Spinner animation="border" role="status">
                                    <span className="visually-hidden">Loading...</span>
                                </Spinner>
                                <p>Loading...</p>
                            </div>
                        )}
                        {error && <p className="text-danger mt-3">{error}</p>}
                        <Link to="/myleagues" className="mt-3 d-block text-center">Back to My Leagues</Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default JoinLeaguePage;
