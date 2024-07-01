import React, { useEffect, useState } from 'react';
import axios, { AxiosError } from 'axios';
import { useNavigate, useParams } from 'react-router-dom';
import { Row, Col, Tabs, Tab, Table, Pagination, Card, Spinner } from 'react-bootstrap';
import '../assets/css/Pagination.css';
import '../assets/css/Tab.css';
import '../assets/css/Row.css';

interface TeamDataDto {
    id: number;
    teamName: string;
    managerId: number;
    managerUsername: string;
    leagueId: number;
    leagueName: string;
    points: number;
    currentPlayers: PlayerDto[];
    pastLineups: PastLineupDto[];
}

interface PlayerDto {
    id: number;
    firstName: string;
    lastName: string;
    position: string;
    clubName: string;
    clubShortName: string;
}

interface PastLineupDto {
    id: number;
    teamId: number;
    gameweek: number;
    season: string;
    startingPlayers: PlayerDto[];
    captainId: number;
    viceCaptainId: number;
    substitutes: Record<number, PlayerDto>;
    powerup: string;
    points: number;
    playerPoints: Record<number, number>;
    playerToPlayedOrNot: Record<number, boolean>;
}

const smallTextStyle = {
    fontSize: '13px',
};

const positionOrder = ['forward', 'midfielder', 'defender', 'goalkeeper'];

const getPlayerPosition = (position: string) => {
    switch (position.toLowerCase()) {
        case 'goalkeeper':
            return 'GK';
        case 'defender':
            return 'DEF';
        case 'midfielder':
            return 'MID';
        case 'forward':
            return 'FWD';
        default:
            return position;
    }
};

const getPowerup = (powerup: string) => {
    switch (powerup.toLowerCase()) {
        case 'bboost':
            return 'Bench Boost';
        case 'cx3':
            return 'Triple Captain';
        default:
            return powerup;
    }
};

const PlayerCard: React.FC<{ player: PlayerDto; points: number; isCaptain: boolean; isViceCaptain: boolean; didPlay: boolean }> = ({ player, points, isCaptain, isViceCaptain, didPlay }) => (
    <Card className="mb-1" style={{ width: '130px', backgroundColor: didPlay ? 'white' : 'gray' }}>
        <Card.Body style={{ padding: '10px' }}>
            <Row className="d-flex justify-content-between align-items-center no-gutters">
                <Col xs={8}>
                    <div style={{ fontSize: '14px' }}>
                        {player.firstName ? `${player.firstName.charAt(0)}. ` : ''}{player.lastName}
                    </div>
                    <div style={{ fontSize: '12px' }}>
                        {getPlayerPosition(player.position)}
                    </div>
                    <div style={{ fontSize: '12px' }}>
                        {player.clubShortName}
                    </div>
                </Col>
                <Col xs={4} className="text-end">
                    <div style={{ fontSize: '14px' }}>{points}</div>
                </Col>
            </Row>
            {isCaptain && <span className="badge bg-primary" style={{ position: 'absolute', top: '5px', right: '5px' }}>C</span>}
            {isViceCaptain && <span className="badge bg-warning" style={{ position: 'absolute', top: '5px', right: '5px' }}>V</span>}
        </Card.Body>
    </Card>
);

const PastLineupNavigation: React.FC<{ pastLineups: PastLineupDto[]; currentGameweek: number; }> = ({ pastLineups, currentGameweek }) => {
    const [activePage, setActivePage] = useState(1);

    const handleFirstPage = () => {
        setActivePage(1);
    };

    const handlePreviousPage = () => {
        if (activePage > 1) {
            setActivePage(activePage - 1);
        }
    };

    const handleNextPage = () => {
        if (activePage < currentGameweek - 1) {
            setActivePage(activePage + 1);
        }
    };

    const handleLastPage = () => {
        setActivePage(currentGameweek - 1);
    };

    const currentLineup = pastLineups.find(lineup => lineup.gameweek === activePage);

    return (
        <div className="flex-grow-1 d-flex flex-column" style={{ overflow: 'auto' }}>
            <div className="d-flex justify-content-between align-items-top" style={{ marginTop: '20px', marginBottom: '0' }}>
                <h5 style={{ fontWeight: 'normal' }}>Past Performance</h5>
                {currentGameweek > 1 && (
                    <Pagination className="justify-content-center">
                        <Pagination.First onClick={handleFirstPage} disabled={activePage === 1} />
                        <Pagination.Prev onClick={handlePreviousPage} disabled={activePage === 1} />
                        <Pagination.Item>{`Gameweek ${activePage}`}</Pagination.Item>
                        <Pagination.Next onClick={handleNextPage} disabled={activePage === currentGameweek - 1} />
                        <Pagination.Last onClick={handleLastPage} disabled={activePage === currentGameweek - 1} />
                    </Pagination>
                )}
            </div>
            <div className="flex-grow-1 d-flex flex-column" style={{ overflow: 'auto' }}>
                {currentGameweek === 1 ? (
                    <div>
                        <h5 style={{ fontWeight: 'normal' }}>No gameweek to display data from.</h5>
                    </div>
                ) : currentLineup ? (
                    <div className="flex-grow-1 d-flex flex-column" style={{ overflow: 'auto' }}>
                        <Row className="mb-3 no-outer-gutters">
                            <Col className="justify-content-left" style={{ padding: 0 }}>
                                <h6 style={{ fontWeight: 'normal' }}>Points: {currentLineup.points}</h6>
                            </Col>
                            <Col className="justify-content-left" style={{ padding: 0 }}>
                                <h6 style={{ fontWeight: 'normal' }}>Powerup: {currentLineup.powerup ? getPowerup(currentLineup.powerup) : 'None'}</h6>
                            </Col>
                        </Row>
                        <h6 className="mb-3" style={{ fontWeight: 'normal' }}>Starting lineup</h6>
                        {positionOrder.map(position => (
                            <Row key={position} className="mb-3 justify-content-center no-outer-gutters">
                                {currentLineup.startingPlayers
                                    .filter(player => player.position.toLowerCase() === position)
                                    .map(player => (
                                        <Col key={player.id} xs={12} sm={6} md={4} lg={3}>
                                            <PlayerCard
                                                player={player}
                                                points={currentLineup.playerPoints[player.id] || 0}
                                                isCaptain={player.id === currentLineup.captainId}
                                                isViceCaptain={player.id === currentLineup.viceCaptainId}
                                                didPlay={currentLineup.playerToPlayedOrNot[player.id] || false}
                                            />
                                        </Col>
                                    ))}
                            </Row>
                        ))}
                        <h6 className="mb-3" style={{ fontWeight: 'normal' }}>Substitutes</h6>
                        <Row className="mb-3 justify-content-center no-outer-gutters">
                            {Object.entries(currentLineup.substitutes)
                                .sort(([a], [b]) => parseInt(a) - parseInt(b))
                                .map(([substitutePosition, player]) => (
                                    <Col key={player.id} xs={12} sm={6} md={4} lg={3}>
                                        <PlayerCard
                                            player={player}
                                            points={currentLineup.playerPoints[player.id] || 0}
                                            isCaptain={player.id === currentLineup.captainId}
                                            isViceCaptain={player.id === currentLineup.viceCaptainId}
                                            didPlay={currentLineup.playerToPlayedOrNot[player.id] || false}
                                        />
                                        <h6 className="mb-3" style={{ fontWeight: 'normal', fontSize: '13px' }}>Substitute {substitutePosition}</h6>
                                    </Col>
                                ))}
                        </Row>
                    </div>
                ) : (
                    <div>
                        <h6 style={{ fontWeight: 'normal' }}>No lineup for this gameweek.</h6>
                    </div>
                )}
            </div>
        </div>
    );
};

const TeamDataPage: React.FC = () => {
    const navigate = useNavigate();
    const { league_id } = useParams<{ league_id: string }>();

    const [leagueStatus, setLeagueStatus] = useState<string | null>(null);
    const [teamData, setTeamData] = useState<TeamDataDto[]>([]);
    const [currentGameweek, setCurrentGameweek] = useState<number>(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchLeagueStatus = async () => {
            try {
                const token = localStorage.getItem('token');
                if (!token) {
                    navigate('/login');
                    return;
                }
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

                const response = await axios.get(`/api/league/status/${league_id}`);
                setLeagueStatus(response.data.leagueStatus);
                setCurrentGameweek(response.data.currentGameweek);

                if (['in season', 'post-season'].includes(response.data.leagueStatus)) {
                    fetchTeamData();
                } else {
                    setLoading(false);
                }
            } catch (error: any) {
                handleFetchError(error);
            }
        };

        const fetchTeamData = async () => {
            try {
                const token = localStorage.getItem('token');
                if (!token) {
                    navigate('/login');
                    return;
                }
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

                const response = await axios.get(`/api/team/team_data?league_id=${league_id}`);
                setTeamData(response.data);
                setLoading(false);
            } catch (error: any) {
                handleFetchError(error);
            }
        };

        fetchLeagueStatus();
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

    const renderLoadingAndError = () => {
        if (loading) {
            return (
                <div className="text-center">
                    <Spinner animation="border" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </Spinner>
                    <p>Loading...</p>
                </div>
            );
        }
        if (error) {
            return <div className="alert alert-danger">{error}</div>;
        }
        return null;
    };

    const renderTeamData = () => {
        switch (leagueStatus) {
            case 'created':
            case 'waiting for draft':
            case 'drafting':
                return (
                    <div>
                        <h5 style={{ fontWeight: 'normal' }}>The season has not started.</h5>
                    </div>
                );
            case 'in season':
            case 'post-season':
                return (
                    <Tabs defaultActiveKey="0" id="team-tabs">
                        {teamData.map((team, index) => (
                            <Tab key={index} eventKey={index.toString()} title={`${team.teamName} - ${team.managerUsername}`} style={{ flexGrow: 1, overflow: 'auto' }}>
                                <Row className="no-outer-gutters" style={{ flexGrow: 1, overflow: 'auto' }}>
                                    <Col xs={12} md={4} className="d-flex flex-column" style={{ overflow: 'auto', maxHeight: '100%' }}>
                                        <h5 style={{ marginTop: '20px', fontWeight: 'normal' }}>Point: {team.points}</h5>
                                        <h5 style={{ marginTop: '20px', fontWeight: 'normal' }}>Current Players</h5>
                                        <Table striped responsive hover>
                                            <thead>
                                                <tr>
                                                    <th>Name</th>
                                                    <th>Position</th>
                                                    <th>Club</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {team.currentPlayers.map(player => (
                                                    <tr key={player.id}>
                                                        <td style={smallTextStyle}>{player.firstName} {player.lastName}</td>
                                                        <td style={smallTextStyle}>{getPlayerPosition(player.position)}</td>
                                                        <td style={smallTextStyle}>{player.clubName}</td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </Table>
                                    </Col>
                                    <Col xs={12} md={8} className="d-flex flex-column" style={{ overflow: 'auto', maxHeight: '100%' }}>
                                        <PastLineupNavigation pastLineups={team.pastLineups} currentGameweek={currentGameweek} />
                                    </Col>
                                </Row>
                            </Tab>
                        ))}
                    </Tabs>
                );
            default:
                return null;
        }
    };

    return (
        <div className="flex-grow-1 d-flex flex-column p-5" style={{ overflow: 'auto' }}>
            <h2 style={{ marginBottom: '20px' }}>Teams</h2>
            {renderLoadingAndError()}
            {!loading && !error && renderTeamData()}
        </div>
    );
};

export default TeamDataPage;