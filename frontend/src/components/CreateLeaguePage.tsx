import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
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

interface LeagueCreationRequestDto {
    name: string;
    scoringRule: ScoringRuleDto;
    powerUps: { [key: string]: number };
    maxPlayersFromSameClub: number;
    teamName: string;
}

const customLabels: { [key: string]: string } = {
    for60Mins: "Points for playing up to 60 minutes",
    forOver60Mins: "Points for playing over 60 minutes",
    forGkDfGoal: "Points for a goal by a GK or DEF",
    forMdGoal: "Points for a goal by a MID",
    forFwGoal: "Points for a goal by a FWD",
    forAssist: "Points for an assist",
    forGkDfCleanSheet: "Points for a clean sheet by a GK or DEF",
    forMdCleanSheet: "Points for a clean sheet by a MID",
    for3GkSaves: "Points for each 3 GK saves",
    forPkSaved: "Points for a penalty saved",
    forPkMissed: "Points for a penalty missed",
    for2GoalsConceded: "Points for each 2 goals conceded by a GK or DEF",
    forYellowCard: "Points for a yellow card",
    forRedCard: "Points for a red card",
    forOwnGoal: "Points for an own goal",
    bboost: "Bench Boost",
    cx3: "Triple Captain",
};

const CreateLeaguePage: React.FC = () => {
    const [leagueName, setLeagueName] = useState('');
    const [scoringRule, setScoringRule] = useState<ScoringRuleDto>({
        for60Mins: 0,
        forOver60Mins: 0,
        forGkDfGoal: 0,
        forMdGoal: 0,
        forFwGoal: 0,
        forAssist: 0,
        forGkDfCleanSheet: 0,
        forMdCleanSheet: 0,
        for3GkSaves: 0,
        forPkSaved: 0,
        forPkMissed: 0,
        for2GoalsConceded: 0,
        forYellowCard: 0,
        forRedCard: 0,
        forOwnGoal: 0
    });
    const [powerUps, setPowerUps] = useState<{ [key: string]: number }>({
        bboost: 0,
        cx3: 0
    });
    const [maxPlayersFromSameClub, setMaxPlayersFromSameClub] = useState<number>(0);
    const [teamName, setTeamName] = useState<string>('');
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/login');
        }
    }, [navigate]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { id, value } = e.target;
        let numericValue = parseInt(value, 10);

        if (numericValue > 10) numericValue = 10;
        if (numericValue < -10) numericValue = -10;

        setScoringRule(prevState => ({
            ...prevState,
            [id]: numericValue
        }));
    };

    const validateScoringRule = () => {
        for (const key in scoringRule) {
            const value = scoringRule[key as keyof ScoringRuleDto];
            if (isNaN(value) || value < -10 || value > 10) {
                return `Field ${key} must be between -10 and 10.`;
            }
        }
        return null;
    };

    const handlePowerUpChange = (key: string, value: number) => {
        setPowerUps(prevState => ({
            ...prevState,
            [key]: value
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!leagueName.trim()) {
            setError('League name is required');
            return;
        }

        if (!teamName.trim()) {
            setError('Your team name is required');
            return;
        }

        const validationError = validateScoringRule();
        if (validationError) {
            setError(validationError);
            return;
        }

        const requestData: LeagueCreationRequestDto = {
            name: leagueName,
            scoringRule: scoringRule,
            powerUps: powerUps,
            maxPlayersFromSameClub: maxPlayersFromSameClub,
            teamName: teamName
        };

        try {
            setLoading(true);
            const response = await axios.post('/api/league/create', requestData, {
                headers: {
                    Authorization: `Bearer ${localStorage.getItem('token')}` // Include JWT token in request headers
                }
            });

            if (response.status === 200) {
                navigate('/myleagues');
            }
        } catch (error) {
            if (axios.isAxiosError(error)) {
                const axiosError = error as AxiosError;
                if (axiosError.response?.status === 401) {
                    navigate('/login'); // Redirect to login if unauthorized
                } else {
                    const errorMessage = axiosError.response?.data?.toString() || 'Failed to create league';
                    setError(errorMessage);
                }
            } else {
                setError('An unexpected error occurred');
            }
        } finally {
            setLoading(false);
        }
    };

    const renderPowerUpsFields = () => {
        return (
            <div className="row">
                {['bboost', 'cx3'].map((key) => (
                    <div key={key} className="col-md-6 mb-3">
                        <label htmlFor={key} className="form-label">{customLabels[key]}</label>
                        <input
                            type="number"
                            id={key}
                            value={powerUps[key] ?? 0}
                            onChange={(e) => handlePowerUpChange(key, parseInt(e.target.value, 10))}
                            className="form-control"
                            required
                            min="0"
                            max="10"
                        />
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div className="container">
            <div className="row justify-content-center m-5">
                <div className="col-md-9">
                    <div className="card p-4">
                        <h2 className="text-center mb-4">Create League</h2>
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <h5>League Name:</h5>
                                <input
                                    type="text"
                                    value={leagueName}
                                    onChange={(e) => setLeagueName(e.target.value)}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <h5 className="mb-3">Scoring Rule:</h5>
                            <div className="row">
                                {Object.keys(scoringRule).map((key) => (
                                    <div key={key} className="col-md-6 mb-3">
                                        <label htmlFor={key} className="form-label">{customLabels[key]}</label>
                                        <input
                                            type="number"
                                            id={key}
                                            value={scoringRule[key as keyof ScoringRuleDto]}
                                            onChange={handleInputChange}
                                            className="form-control"
                                            required
                                            min="-10"
                                            max="10"
                                        />
                                    </div>
                                ))}
                            </div>
                            <h5 className="mb-3">Power-Ups:</h5>
                            {renderPowerUpsFields()}
                            <h5 className="mb-3">Other Settings:</h5>
                            <div className="mb-3">
                                <label htmlFor="maxPlayersFromSameClub" className="form-label">
                                    Maximum number of players from the same club allowed in a team
                                </label>
                                <input
                                    type="number"
                                    id="maxPlayersFromSameClub"
                                    value={maxPlayersFromSameClub}
                                    onChange={(e) => setMaxPlayersFromSameClub(parseInt(e.target.value, 10))}
                                    className="form-control"
                                    required
                                    min="0"
                                    max="11"
                                />
                            </div>
                            <div className="mb-3">
                                <label htmlFor="teamName" className="form-label">Your Team Name</label>
                                <input
                                    type="text"
                                    id="teamName"
                                    value={teamName}
                                    onChange={(e) => setTeamName(e.target.value)}
                                    className="form-control"
                                    required
                                />
                            </div>
                            <button type="submit" className="btn btn-primary">Create</button>
                        </form>
                        {loading && (
                            <div className="text-center mt-3">
                                <Spinner animation="border" role="status">
                                    <span className="visually-hidden">Loading...</span>
                                </Spinner>
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

export default CreateLeaguePage;