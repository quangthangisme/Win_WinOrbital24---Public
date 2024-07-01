import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios, { AxiosError } from 'axios';
import { Spinner } from 'react-bootstrap';

interface League {
    id: number;
    name: string;
    code: string;
}

const MyLeaguesPage: React.FC = () => {
    const navigate = useNavigate();
    const [leagues, setLeagues] = useState<League[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        const fetchLeagues = async () => {
            try {
                const token = localStorage.getItem('token');

                if (!token) {
                    navigate('/login');
                    return;
                }

                const leaguesResponse = await axios.get('/api/manager/myleagues', {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                });

                if (leaguesResponse.status !== 200) {
                    const errorMessage = leaguesResponse.data || 'Failed to fetch leagues';
                    setError(errorMessage);
                    setLoading(false);
                    return;
                }

                setLeagues(leaguesResponse.data);
                setLoading(false);
            } catch (error: any) {
                handleFetchError(error);
            }
        };

        fetchLeagues();
    }, [navigate]);

    const handleFetchError = (error: AxiosError) => {
        if (error.response) {
            console.error('Error response:', error.response);
            if (error.response.status === 401) {
                navigate('/login');
            } else {
                const errorMessage = typeof error.response.data === 'string' ? error.response.data : 'An unexpected error occurred';
                setError(errorMessage);
            }
        } else if (error.request) {
            console.error('Error request:', error.request);
            setError('No response from server');
        } else {
            console.error('Error message:', error.message);
            setError('An unexpected error occurred');
        }
        setLoading(false);
    };

    return (
            <div className="container">
                <div className="my-leagues-container mt-5">
                    <div className="d-flex justify-content-between align-items-center mb-4">
                        <h2 className="mb-0">My Leagues</h2>
                    </div>
                    {loading ? (
                        <div className="text-center">
                            <Spinner animation="border" role="status">
                                <span className="visually-hidden">Loading...</span>
                            </Spinner>
                            <p>Loading leagues...</p>
                        </div>
                    ) : error ? (
                        <p className="error-message text-center">Error: {error}</p>
                    ) : (
                        <div>
                            <table className="table table-striped">
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Code</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {leagues.map((league) => (
                                        <tr key={league.id}>
                                            <td><Link to={`/league/${league.id}`}>{league.name}</Link></td>
                                            <td>{league.code}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                            <div className="d-flex justify-content-center">
                                <Link to="/create_league" className="btn btn-primary me-3">Create League</Link>
                                <Link to="/join_league" className="btn btn-secondary">Join League</Link>
                            </div>
                        </div>
                    )}
                </div>
            </div>
    );
};

export default MyLeaguesPage;