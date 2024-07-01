import { useParams, useNavigate } from 'react-router-dom';
import axios, { AxiosError } from 'axios';
import 'bootstrap/dist/css/bootstrap.min.css';
import React, { useState, useEffect } from 'react';
import { Button, Card, Col, Form, Row, Spinner } from 'react-bootstrap';
import { DndContext, DragOverlay, DragStartEvent } from '@dnd-kit/core';
import { restrictToWindowEdges } from '@dnd-kit/modifiers';
import { SortableContext, useSortable, rectSwappingStrategy, arrayMove } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import '../assets/css/Row.css';

interface LineupDto {
    startingPlayerIds: number[];
    captainId: number;
    viceCaptainId: number;
    substitutes: { [key: number]: number };
    powerup: string | null;
}

interface PlayerDto {
    id: number;
    firstName: string;
    lastName: string;
    position: string;
    clubName: string;
    clubShortName: string;
}

const abbreviatePosition = (position: string) => {
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

const getInitial = (name: string) => {
    return name ? `${name.charAt(0)}.` : '';
};

const PlayerCard: React.FC<{ player: PlayerDto }> = ({ player }) => (
    <Card style={{ width: '100px', padding: '5px' }}>
        <Card.Body style={{ padding: '5px' }}>
            <Card.Title style={{ fontSize: '14px', margin: '5px 0' }}>
                {`${getInitial(player.firstName)} ${player.lastName}`}
            </Card.Title>
            <Card.Text style={{ fontSize: '12px', margin: '5px 0' }}>{player.clubShortName}</Card.Text>
            <Card.Text style={{ fontSize: '12px' }}>{abbreviatePosition(player.position)}</Card.Text>
        </Card.Body>
    </Card>
);

const SortablePlayerCard: React.FC<{ player: PlayerDto, id: number, isDragging?: boolean; }> = ({ player, id, isDragging }) => {
    const { attributes, listeners, setNodeRef, transform, transition } = useSortable({
        id,
        transition: {
            duration: 500,
            easing: "cubic-bezier(0.25, 1, 0.5, 1)",
        },
    });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition: isDragging ? transition : undefined,
        cursor: isDragging ? "grabbing" : "grab",
    };

    return (
        <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
            <PlayerCard player={player} />
        </div>
    );
};

const initialLineup: LineupDto = {
    startingPlayerIds: [],
    captainId: 0,
    viceCaptainId: 0,
    substitutes: {},
    powerup: null,
};

const MyTeamPage: React.FC = () => {

    const navigate = useNavigate();
    const { league_id } = useParams<{ league_id: string }>();
    const [players, setPlayers] = useState<PlayerDto[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [lineupError, setLineupError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [remainingPowerups, setRemainingPowerups] = useState({ bboost: 0, cx3: 0 });
    const [lineup, setLineup] = useState<LineupDto>(initialLineup);
    const [formation, setFormation] = useState<string>('');
    const [activePlayer, setActivePlayer] = useState<PlayerDto | null>(null);
    const [existingLineup, setExistingLineup] = useState(false);
    const [leagueStatus, setLeagueStatus] = useState<string>('');

    const findValidLineup = (players: PlayerDto[]): LineupDto => {
        const validLineup: LineupDto = { ...initialLineup };

        // Separate players by position
        const goalkeepers = players.filter(player => player.position === 'goalkeeper');
        const defenders = players.filter(player => player.position === 'defender');
        const midfielders = players.filter(player => player.position === 'midfielder');
        const forwards = players.filter(player => player.position === 'forward');

        // Ensure there's exactly one goalkeeper, at least three defenders, and at least one forward
        if (goalkeepers.length < 1 || defenders.length < 3 || forwards.length < 1) {
            setLineupError('Invalid player configuration: Not enough players in required positions.');
        }

        // Add one goalkeeper
        validLineup.startingPlayerIds.push(goalkeepers[0].id);

        // Add three defenders
        for (let i = 0; i < Math.min(3, defenders.length); i++) {
            validLineup.startingPlayerIds.push(defenders[i].id);
        }

        // Add one forward
        validLineup.startingPlayerIds.push(forwards[0].id);

        // Add remaining required players
        let remainingRequiredPlayersCount = 11;
        const remainingDefenders = defenders.slice(3);
        const remainingMidfielders = midfielders.slice(0); // -1 for the forward already added
        const remainingForwards = forwards.slice(1); // Skip the first forward already added

        // Add remaining defenders
        remainingRequiredPlayersCount = 11 - validLineup.startingPlayerIds.length;
        for (let i = 0; i < Math.min(remainingRequiredPlayersCount, remainingDefenders.length); i++) {
            validLineup.startingPlayerIds.push(remainingDefenders[i].id);
        }

        // Add remaining midfielders
        remainingRequiredPlayersCount = 11 - validLineup.startingPlayerIds.length;
        for (let i = 0; i < Math.min(remainingRequiredPlayersCount, remainingMidfielders.length); i++) {
            validLineup.startingPlayerIds.push(remainingMidfielders[i].id);
        }

        // Add remaining forwards
        remainingRequiredPlayersCount = 11 - validLineup.startingPlayerIds.length;
        for (let i = 0; i < Math.min(remainingRequiredPlayersCount, remainingForwards.length); i++) {
            validLineup.startingPlayerIds.push(remainingForwards[i].id);
        }

        // Assign captain and vice-captain (for demonstration purpose, assigning the first player as captain and the second player as vice-captain)
        validLineup.captainId = validLineup.startingPlayerIds[0];
        validLineup.viceCaptainId = validLineup.startingPlayerIds[1];

        const remainingPlayers = players.filter(player => !validLineup.startingPlayerIds.includes(player.id));
        for (let i = 0; i < remainingPlayers.length; i++) {
            validLineup.substitutes[i + 1] = remainingPlayers[i].id; // assigning substitute number starting from 1
        }

        return validLineup;
    };

    const calculateFormationFromLineup = (lineup: LineupDto, fetchedPlayers: PlayerDto[]): string => {
        const formationParts: string[] = [];
        const defendersCount = lineup.startingPlayerIds.filter(playerId =>
            fetchedPlayers.find(player => player.id === playerId)?.position === 'defender'
        ).length;
        const midfieldersCount = lineup.startingPlayerIds.filter(playerId =>
            fetchedPlayers.find(player => player.id === playerId)?.position === 'midfielder'
        ).length;
        const forwardsCount = lineup.startingPlayerIds.filter(playerId =>
            fetchedPlayers.find(player => player.id === playerId)?.position === 'forward'
        ).length;

        formationParts.push(defendersCount.toString());
        formationParts.push(midfieldersCount.toString());
        formationParts.push(forwardsCount.toString());

        return formationParts.join('-');
    };

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

                if ('in season' == response.data.leagueStatus) {
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

                const playersResponse = await axios.get(`/api/team/current_players/curr_manager?league_id=${league_id}`);
                const fetchedPlayers = playersResponse.data;
                setPlayers(fetchedPlayers);

                const lineupResponse = await axios.get(`/api/lineup/current?league_id=${league_id}`);
                const currentLineup = lineupResponse.data;

                if (currentLineup.startingPlayerIds) {
                    setExistingLineup(true);
                    setLineup(currentLineup);
                    setFormation(calculateFormationFromLineup(currentLineup, fetchedPlayers));
                }

                const powerupsResponse = await axios.get(`/api/team/remaining_powerups/curr_manager?league_id=${league_id}`);
                setRemainingPowerups(powerupsResponse.data);

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
    };

    useEffect(() => {
        if (players.length > 0) {
            try {
                // Check if there's already a current lineup available
                if (lineup.startingPlayerIds.length === 0) {
                    // If no current lineup exists, find a valid lineup
                    const validLineup = findValidLineup(players);
                    setLineup(validLineup);

                    const validFormation = calculateFormationFromLineup(validLineup, players);
                    setFormation(validFormation);
                }
            } catch (error) {
                setError("Error fetching players: " + error);
            }
        }
    }, [players, lineup]);

    const validateFormation = (inputFormation: string): boolean => {
        const regex = /^\d{1,2}-\d{1,2}-\d{1,2}$/;
        if (!regex.test(inputFormation)) {
            setLineupError('Invalid formation format. Please use format like "4-3-3".');
            return false;
        }

        const [defenders, midfielders, forwards] = inputFormation.split('-').map(Number);
        if (defenders < 3 || forwards < 1 || defenders + midfielders + forwards !== 10) {
            setLineupError('Invalid formation.');
            return false;
        }

        return true;
    };

    const applyFormation = () => {
        if (validateFormation(formation)) {
            const [defenders, midfielders, forwards] = formation.split('-').map(Number);

            const goalkeepers = players.filter(player => player.position === 'goalkeeper');
            const availableDefenders = players.filter(player => player.position === 'defender');
            const availableMidfielders = players.filter(player => player.position === 'midfielder');
            const availableForwards = players.filter(player => player.position === 'forward');

            if (
                goalkeepers.length < 1 ||
                availableDefenders.length < defenders ||
                availableMidfielders.length < midfielders ||
                availableForwards.length < forwards
            ) {
                setLineupError('Formation is not possible.');
                return;
            }

            const startingPlayers = [
                goalkeepers[0].id,
                ...availableDefenders.slice(0, defenders).map(player => player.id),
                ...availableMidfielders.slice(0, midfielders).map(player => player.id),
                ...availableForwards.slice(0, forwards).map(player => player.id),
            ];

            const selectedPlayers = new Set(startingPlayers);
            const substitutes = players
                .filter(player => !selectedPlayers.has(player.id))
                .map(player => player.id);

            const substitutesObj: { [key: number]: number } = substitutes.reduce((acc: { [key: number]: number }, playerId, index) => {
                acc[index + 1] = playerId;
                return acc;
            }, {});

            setLineup(prevLineup => ({
                ...prevLineup,
                startingPlayerIds: startingPlayers,
                substitutes: substitutesObj,
            }));

            setLineupError(null);
        }
    };

    const handleFormationChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setFormation(event.target.value);
    };

    const handleDragStart = (event: DragStartEvent) => {
        const { active } = event;
        const foundPlayer = players.find((player) => player.id === active.id);
        if (foundPlayer) {
            setActivePlayer(foundPlayer);
        }
    };

    const handleDragEnd = (event: any) => {
        const { active, over } = event;
        if (over) {
            if (active.id !== over.id) {
                const activePlayer = players.find(p => p.id === active.id);
                const overPlayer = players.find(p => p.id === over.id);

                if (!activePlayer || !overPlayer) return;

                const activeInStarting = lineup.startingPlayerIds.includes(active.id);
                const overInStarting = lineup.startingPlayerIds.includes(over.id);

                if (activeInStarting && overInStarting) {
                    if (activePlayer.position === overPlayer.position) {
                        const newStartingPlayerIds = arrayMove(lineup.startingPlayerIds, lineup.startingPlayerIds.indexOf(active.id), lineup.startingPlayerIds.indexOf(over.id));
                        setLineup({ ...lineup, startingPlayerIds: newStartingPlayerIds });
                    }
                } else if (activeInStarting || overInStarting) {
                    if (activePlayer.position === overPlayer.position) {
                        let newStartingPlayerIds = [...lineup.startingPlayerIds];
                        let newSubstitutes = { ...lineup.substitutes };
                        let newCaptain = lineup.captainId;
                        let newViceCaptain = lineup.viceCaptainId;

                        if (activeInStarting) {
                            const activeIndex = newStartingPlayerIds.indexOf(active.id);
                            newStartingPlayerIds.splice(activeIndex, 1, over.id);

                            const newSubstituteKey = Object.keys(newSubstitutes).find((key: string) => newSubstitutes[parseInt(key)] === over.id);
                            if (newSubstituteKey) {
                                newSubstitutes[parseInt(newSubstituteKey)] = active.id;
                            }

                            if (lineup.captainId === active.id) {
                                newCaptain = over.id;
                            }
                            if (lineup.viceCaptainId === active.id) {
                                newViceCaptain = over.id;
                            }

                        } else {
                            const overIndex = newStartingPlayerIds.indexOf(over.id);
                            newStartingPlayerIds.splice(overIndex, 1, active.id);

                            const newSubstituteKey = Object.keys(newSubstitutes).find((key: string) => newSubstitutes[parseInt(key)] === active.id);
                            if (newSubstituteKey) {
                                newSubstitutes[parseInt(newSubstituteKey)] = over.id;
                            }

                            if (lineup.captainId === over.id) {
                                newCaptain = active.id;
                            }
                            if (lineup.viceCaptainId === over.id) {
                                newViceCaptain = active.id;
                            }

                        }

                        setLineup({ ...lineup, startingPlayerIds: newStartingPlayerIds, substitutes: newSubstitutes, captainId: newCaptain, viceCaptainId: newViceCaptain });
                    }
                } else {
                    const newSubstitutes = { ...lineup.substitutes };
                    const activeSubstituteKey = Object.keys(newSubstitutes).find((key: string) => newSubstitutes[parseInt(key)] === active.id);
                    const overSubstituteKey = Object.keys(newSubstitutes).find((key: string) => newSubstitutes[parseInt(key)] === over.id);

                    if (activeSubstituteKey && overSubstituteKey) {
                        const temp = newSubstitutes[parseInt(activeSubstituteKey)];
                        newSubstitutes[parseInt(activeSubstituteKey)] = newSubstitutes[parseInt(overSubstituteKey)];
                        newSubstitutes[parseInt(overSubstituteKey)] = temp;

                        setLineup({ ...lineup, substitutes: newSubstitutes });
                    }
                }
            }
        }
    };

    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        try {
            await axios.post(`/api/lineup/submit?league_id=${league_id}`, lineup);
            window.location.reload();
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                setError(error.response.data);
            } else {
                setError('An unexpected error occurred.');
            }
        }
    };

    const handlePowerupChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const powerupValue = event.target.value;
        setLineup(prevLineup => ({
            ...prevLineup,
            powerup: prevLineup.powerup === powerupValue ? null : powerupValue,
        }));
    };

    const renderPlayersByPosition = (position: string, isSubstitute?: boolean) => {
        const playerIds = isSubstitute ? Object.values(lineup.substitutes) : lineup.startingPlayerIds;
        return playerIds.map(playerId => {
            const player = players.find(p => p.id === playerId && p.position === position);
            if (player) {
                return (
                    <Col key={player.id} style={{ padding: '5px', display: 'flex', justifyContent: 'center' }}>
                        <SortablePlayerCard player={player} id={player.id} />
                    </Col>
                );
            }
            return null;
        });
    };

    useEffect(() => {
        if (lineup.captainId === lineup.viceCaptainId) {
            setLineupError('Captain and vice captain cannot be the same.');
        } else {
            setLineupError(null);
        }
    }, [lineup.captainId, lineup.viceCaptainId]);

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

    const renderLineup = () => {
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
                return (
                    <div className="d-flex flex-column flex-grow-1" style={{ overflow: 'auto' }}>
                        <Row className="no-outer-gutters">
                            <Col xs={12}>
                                <Form onSubmit={handleSubmit}>
                                    <Row>
                                        <Col xs="auto">
                                            <Form.Group controlId="formation">
                                                <Form.Label>Formation</Form.Label>
                                                <Form.Control
                                                    type="text"
                                                    placeholder="Formation"
                                                    value={formation}
                                                    onChange={handleFormationChange}
                                                    style={{ width: '120px' }}
                                                />
                                            </Form.Group>
                                        </Col>
                                        <Col xs="auto" className="d-flex align-items-center justify-content-start">
                                            <Button variant="primary" onClick={applyFormation}>Apply</Button>
                                        </Col>
                                        <Col className="d-flex align-items-center justify-content-start">
                                            {lineupError && <p style={{ color: 'red', marginTop: '10px', marginLeft: '20px', marginBottom: 0 }}>{error}</p>}
                                        </Col>
                                        <Col className="d-flex flex-grow-1 align-items-center justify-content-end">
                                            <Button variant={existingLineup ? "success" : "primary"} type="submit">
                                                {existingLineup ? "Edit" : "Submit"}
                                            </Button>
                                        </Col>
                                    </Row>
                                </Form>
                            </Col>
                        </Row>
                        <Row style={{ marginTop: '10px', overflow: 'auto' }} className="d-flex flex-grow-1 no-outer-gutters">
                            <Col xs={7} style={{ height: '100%', overflow: 'auto' }} className='d-flex flex-column'>
                                <h6 style={{ fontWeight: 'normal' }} className="mb-3">Starting Lineup</h6>
                                <div className="d-flex flex-grow-1 flex-column" style={{ overflow: 'auto' }}>
                                    <Row className="justify-content-center flex-wrap flex-grow-1" style={{ width: '100%', marginBottom: '1.5rem' }}>
                                        {renderPlayersByPosition('forward')}
                                    </Row>
                                    <Row className="justify-content-center flex-wrap flex-grow-1" style={{ width: '100%', marginBottom: '1.5rem' }}>
                                        {renderPlayersByPosition('midfielder')}
                                    </Row>
                                    <Row className="justify-content-center flex-wrap flex-grow-1" style={{ width: '100%', marginBottom: '1.5rem' }}>
                                        {renderPlayersByPosition('defender')}
                                    </Row>
                                    <Row className="justify-content-center flex-wrap flex-grow-1" style={{ width: '100%' }}>
                                        {renderPlayersByPosition('goalkeeper')}
                                    </Row>
                                </div>
                            </Col>
                            <Col xs={5} style={{ height: '100%', overflow: 'auto' }} className='d-flex flex-column'>
                                <Form style={{ height: '100%' }} className='d-flex flex-column'>
                                    <Form.Group controlId="captains" style={{ marginRight: '3rem' }}>
                                        <Form.Label>Captain</Form.Label>
                                        <Form.Control style={{ width: '300px', marginBottom: '1rem' }} as="select" value={lineup.captainId} onChange={(e) => setLineup({ ...lineup, captainId: parseInt(e.target.value) })}>
                                            {lineup.startingPlayerIds.map(playerId => {
                                                const player = players.find(p => p.id === playerId);
                                                return <option key={playerId} value={playerId}>{`${player?.firstName} ${player?.lastName}`}</option>;
                                            })}
                                        </Form.Control>
                                        <Form.Label>Vice Captain</Form.Label>
                                        <Form.Control style={{ width: '300px', marginBottom: '1rem' }} as="select" value={lineup.viceCaptainId} onChange={(e) => setLineup({ ...lineup, viceCaptainId: parseInt(e.target.value) })}>
                                            {lineup.startingPlayerIds.map(playerId => {
                                                const player = players.find(p => p.id === playerId);
                                                return <option key={playerId} value={playerId}>{`${player?.firstName} ${player?.lastName}`}</option>;
                                            })}
                                        </Form.Control>
                                    </Form.Group>
                                    <Form.Group controlId="powerups" style={{ marginRight: '3rem', marginBottom: '1rem' }}>
                                        <Form.Label>Powerups</Form.Label>
                                        <Form.Check
                                            type="checkbox"
                                            label={`Bench Boost (x${remainingPowerups.bboost} left)`}
                                            value="bboost"
                                            checked={lineup.powerup === "bboost"}
                                            onChange={handlePowerupChange}
                                            disabled={remainingPowerups.bboost === 0}
                                        />
                                        <Form.Check
                                            type="checkbox"
                                            label={`Triple Captain (x${remainingPowerups.cx3} left)`}
                                            value="cx3"
                                            checked={lineup.powerup === "cx3"}
                                            onChange={handlePowerupChange}
                                            disabled={remainingPowerups.cx3 === 0}
                                        />
                                    </Form.Group>
                                    <Form.Group controlId="substitutes" style={{ display: 'flex', flexDirection: 'column', flexGrow: 1, overflow: 'auto' }}>
                                        <h6 style={{ fontWeight: 'normal' }} className="mb-3">Substitute Players</h6>
                                        <div style={{ display: 'flex', flexGrow: 1, overflow: 'auto' }}>
                                            <Row className="justify-content-center flex-wrap" style={{ marginBottom: '10px', width: '100%' }}>
                                                {Object.entries(lineup.substitutes).map(([order, playerId]) => {
                                                    const substitutePlayer = players.find(player => player.id === playerId);
                                                    if (substitutePlayer) {
                                                        return (
                                                            <Col key={order} xs="auto" className="d-flex flex-column align-items-center">
                                                                <SortablePlayerCard player={substitutePlayer} id={substitutePlayer.id} />
                                                                <p style={{ fontSize: '13px', marginTop: '0.5rem' }}>Substitute {order}</p>
                                                            </Col>
                                                        );
                                                    }
                                                    return null;
                                                })}
                                            </Row>
                                        </div>
                                    </Form.Group>
                                </Form>
                            </Col>
                        </Row>
                    </div>
                );
            case 'post-season':
                return (
                    <div>
                        <h5 style={{ fontWeight: 'normal' }}>The season has ended.</h5>
                    </div>
                );
            default:
                return null;
        }
    };

    return (
        <div className="d-flex flex-column flex-grow-1 p-5" style={{ overflow: 'auto' }}>
            <DndContext onDragStart={handleDragStart} onDragEnd={handleDragEnd} modifiers={[restrictToWindowEdges]}>
                <SortableContext items={[...lineup.startingPlayerIds, ...Object.values(lineup.substitutes)]} strategy={rectSwappingStrategy}>
                    <h2 style={{ marginBottom: '20px' }}>My Team</h2>
                    {renderLoadingAndError()}
                    {!loading && !error && renderLineup()}
                </SortableContext>
                <DragOverlay adjustScale style={{ transformOrigin: "0 0 " }}>
                    {activePlayer ? <SortablePlayerCard player={activePlayer} id={activePlayer.id} isDragging /> : null}
                </DragOverlay>
            </DndContext>
        </div>
    );
};

export default MyTeamPage;
