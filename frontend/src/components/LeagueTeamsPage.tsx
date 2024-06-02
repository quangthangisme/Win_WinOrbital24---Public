import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

interface TeamDto {
    teamName: string;
    managerUsername: string;
    points: number;
}

const LeagueTeamsPage: React.FC = () => {
    const { league_id } = useParams();
    const navigate = useNavigate();
    const [teams, setTeams] = useState<TeamDto[]>([]);
    const [leagueName, setLeagueName] = useState<string>('');
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchLeagueData = async () => {
            try {
                const authenticatedResponse = await axios.get('/api/authenticated');
                if (authenticatedResponse.status !== 200) {
                    navigate('/login');
                    return;
                }

                const response = await axios.get(`/api/league/${league_id}`);
                if (response.status !== 200) {
                    const errorMessage = response.data;
                    setError(errorMessage || 'Failed to fetch leagues');
                    return;
                }

                setLeagueName(response.data.length > 0 ? response.data[0].leagueName : '');
                setTeams(response.data);
            } catch (error) {
                setError('An unexpected error occurred');
            }
        };

        fetchLeagueData();
    }, [league_id, navigate]);
    

    const handleLogout = async () => {
        try {
            const response = await axios.get('/api/perform_logout');
            if (response.status === 200) {
                navigate('/login');
            }
        } catch (error) {
            console.error('Failed to logout:', error);
        }
    };

    return (
        <div className="container">
            <div className="my-leagues-container mt-5">
                <div className="d-flex justify-content-between align-items-center mb-4">
                    <h2>{leagueName}</h2>
                    <button onClick={handleLogout} className="btn btn-danger">Logout</button>
                </div>
                {error ? (
                    <p className="error-message text-center">Error: {error}</p>
                ) : (
                    <table className="table table-striped">
                        <thead>
                            <tr>
                                <th>Team Name</th>
                                <th>Manager Username</th>
                                <th>Points</th>
                            </tr>
                        </thead>
                        <tbody>
                            {teams.map((team, index) => (
                                <tr key={index}>
                                    <td>{team.teamName}</td>
                                    <td>{team.managerUsername}</td>
                                    <td>{team.points}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
            <div className="mt-3 text-center">
                <Link to="/myleagues">Back to My Leagues</Link>
            </div>
        </div>
    );
};

export default LeagueTeamsPage;
