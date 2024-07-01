import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios, { AxiosError } from 'axios';
import { Spinner } from 'react-bootstrap';

interface TeamDto {
    id: number;
    teamName: string;
    managerUsername: string;
    points?: number;
}

const LeagueRankingPage: React.FC = () => {
    const { league_id } = useParams<{ league_id: string }>();
    const navigate = useNavigate();
    // const [leagueName, setLeagueName] = useState<string>('');
    const [teams, setTeams] = useState<TeamDto[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        const fetchLeagueData = async () => {
            try {
                const token = localStorage.getItem('token');
                if (!token) {
                    navigate('/login');
                    return;
                }

                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

                const teamsResponse = await axios.get(`/api/league/teams/${league_id}`);
                if (teamsResponse.status !== 200) {
                    setError('Failed to fetch league teams');
                    setLoading(false);
                    return;
                }

                const teamsData = teamsResponse.data;

                const updatedTeams = await Promise.all(
                    teamsData.map(async (team: TeamDto) => {
                        try {
                            const pointsResponse = await axios.get(`/api/team/points?team_id=${team.id}`);
                            if (pointsResponse.status !== 200) {
                                setError(`Failed to fetch points for team ${team.teamName}`);
                                return team;
                            }
                            const points = pointsResponse.data;
                            return { ...team, points };
                        } catch (error) {
                            setError(`Failed to fetch points for team ${team.teamName}`);
                            return team;
                        }
                    })
                );

                updatedTeams.sort((a, b) => (a.points && b.points ? b.points - a.points : 0));

                setTeams(updatedTeams);
                setLoading(false);
            } catch (error: any) {
                handleFetchError(error);
            }
        };

        fetchLeagueData();
    }, [league_id, navigate]);

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
        <div className="flex-grow-1 p-5" style={{ overflow: 'auto' }}>
            <h2>Ranking</h2>
            {loading ? (
                <div className="text-center">
                    <Spinner animation="border" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </Spinner>
                    <p>Loading league ranking...</p>
                </div>
            ) : error ? (
                <p className="error-message text-center">Error: {error}</p>
            ) : (
                <table className="table table-striped">
                    <thead>
                        <tr>
                            <th>Team</th>
                            <th>Manager</th>
                            <th>Points</th>
                        </tr>
                    </thead>
                    <tbody>
                        {teams.map((team, index) => (
                            <tr key={index}>
                                <td>{team.teamName}</td>
                                <td>{team.managerUsername}</td>
                                <td>{team.points ?? 'Loading...'}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
};

export default LeagueRankingPage;