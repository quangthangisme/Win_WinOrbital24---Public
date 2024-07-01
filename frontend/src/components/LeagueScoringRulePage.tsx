import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios, { AxiosError } from 'axios';
import { Spinner } from 'react-bootstrap';

interface ScoringRuleDto {
    for60Mins: number;
    forOver60Mins: number;
    forGkDfGoal: number;
    forMdGoal: number;
    forFwGoal: number;
    forAssist: number;
    forGkDfCleanSheet: number;
    forMdCleanSheet: number;
    for3GkSaves: number;
    forPkSaved: number;
    forPkMissed: number;
    for2GoalsConceded: number;
    forYellowCard: number;
    forRedCard: number;
    forOwnGoal: number;
}

const customLabels: { [key: string]: string } = {
    for60Mins: "Points for playing up to 60 minutes",
    forOver60Mins: "Points for playing over 60 minutes",
    forGkDfGoal: "Points for a goal by a goalkeeper or defender",
    forMdGoal: "Points for a goal by a midfielder",
    forFwGoal: "Points for a goal by a forward",
    forAssist: "Points for an assist",
    forGkDfCleanSheet: "Points for a clean sheet by a goalkeeper or defender",
    forMdCleanSheet: "Points for a clean sheet by a midfielder",
    for3GkSaves: "Points for each 3 goalkeeper saves",
    forPkSaved: "Points for a penalty saved",
    forPkMissed: "Points for a penalty missed",
    for2GoalsConceded: "Points for each 2 goals conceded by a goalkeeper or defender",
    forYellowCard: "Points for a yellow card",
    forRedCard: "Points for a red card",
    forOwnGoal: "Points for an own goal",
    bboost: "Bench Boost",
    cx3: "Triple Captain",
};

const LeagueScoringRulePage: React.FC = () => {
    const { league_id } = useParams<{ league_id: string }>();
    const navigate = useNavigate();
    const [scoringRules, setScoringRules] = useState<ScoringRuleDto | null>(null);
    const [powerups, setPowerups] = useState<{ [key: string]: number } | null>(null);
    const [maxPlayersFromClub, setMaxPlayersFromClub] = useState<number>(0);
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

                const scoringRulesResponse = await axios.get(`/api/league/scoring_rule/${league_id}`);
                if (scoringRulesResponse.status !== 200) {
                    setError('Failed to fetch league scoring rules');
                    setLoading(false);
                    return;
                }
                setScoringRules(scoringRulesResponse.data);

                const powerupsResponse = await axios.get(`/api/league/powerups/${league_id}`);
                if (powerupsResponse.status !== 200) {
                    setError('Failed to fetch league powerups');
                    setLoading(false);
                    return;
                }
                setPowerups(powerupsResponse.data);

                const response = await axios.get(`/api/league/max_players_same_club/${league_id}`);
                if (response.status !== 200) {
                    setError('Failed to fetch league data');
                    setLoading(false);
                    return;
                }
                setMaxPlayersFromClub(response.data);

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
            <h2 style={{ marginBottom: '20px' }}>Rules</h2>
            {loading ? (
                <div className="text-center">
                    <Spinner animation="border" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </Spinner>
                    <p>Loading league data...</p>
                </div>
            ) : error ? (
                <p className="error-message text-center">Error: {error}</p>
            ) : (
                <>
                    <div className="mb-5">
                        <h5>Scoring Rules</h5>
                        <table className="table table-striped">
                            <thead>
                                <tr>
                                    <th>Action</th>
                                    <th>Points</th>
                                </tr>
                            </thead>
                            <tbody>
                                {scoringRules && Object.keys(scoringRules).map((key, index) => (
                                    <tr key={index}>
                                        <td>{customLabels[key]}</td>
                                        <td>{scoringRules[key as keyof ScoringRuleDto]}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                    {powerups && (
                        <div className="mb-5">
                            <h5>Powerups</h5>
                            <table className="table table-striped">
                                <thead>
                                    <tr>
                                        <th>Powerup</th>
                                        <th>Count</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {Object.keys(powerups).map((key, index) => (
                                        <tr key={index}>
                                            <td>{customLabels[key]}</td>
                                            <td>{powerups[key]}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    <div>
                        <h5>Other rules</h5>
                            <table className="table table-striped">
                                <thead>
                                    <tr>
                                        <th>Rule</th>
                                        <th>Value</th>
                                    </tr>
                                </thead>
                                <tbody>
                                        <tr>
                                            <td>Maximum number of players from the same club allowed on the same team</td>
                                            <td>{maxPlayersFromClub}</td>
                                        </tr>
                                </tbody>
                            </table>
                        </div>
                </>
            )}
        </div>
    );
};

export default LeagueScoringRulePage;