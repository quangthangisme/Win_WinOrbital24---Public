import React, { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import axios, { AxiosError } from 'axios';
import { Spinner, Tab, Row, Col, Table, Form, Tabs, Button, Modal } from 'react-bootstrap';
import Select, { MultiValue, StylesConfig } from 'react-select';
import '../assets/css/Tab.css';

interface PlayerDto {
    id: number;
    firstName: string;
    lastName: string;
    position: string;
    clubName: string;
    clubShortName: string;
}

interface ManagerDto {
    id: number;
    username: string;
}

interface DraftState {
    currentManager: ManagerDto;
    nextManager: ManagerDto;
    selectedPlayers: Map<ManagerDto, PlayerDto[]>;
    draftPool: PlayerDto[];
    lastPickMessage: string;
    remainingTime: number;
}

interface ReceivedData {
    currentManager: ManagerDto;
    nextManager: ManagerDto;
    selectedPlayers: { [key: string]: PlayerDto[] };
    draftPool: PlayerDto[];
    lastPickMessage: string;
    remainingTime: number;
}

interface Option {
    value: string;
    label: string;
}

const customReactSelectStyles: StylesConfig<Option, true> = {
    valueContainer: (provided) => ({
        ...provided,
        height: '66px',
        overflowY: 'auto',
    }),
};

const smallTextStyle = {
    fontSize: '13px',
};

const DraftPage: React.FC = () => {
    const navigate = useNavigate();
    const { league_id } = useParams<{ league_id: string }>();
    const [draftState, setDraftState] = useState<DraftState>({
        currentManager: { id: 0, username: "" },
        nextManager: { id: 0, username: "" },
        selectedPlayers: new Map<ManagerDto, PlayerDto[]>(),
        draftPool: [],
        lastPickMessage: "",
        remainingTime: 0
    });
    const token = localStorage.getItem('token');
    const clientRef = useRef<Client | null>(null);
    const [remainingTime, setRemainingTime] = useState<number>(0);
    const timerRef = useRef<number | null>(null);
    const username = localStorage.getItem('username');
    const [validPlayers, setValidPlayers] = useState<PlayerDto[]>([]);
    const [maxPlayersFromClub, setMaxPlayersFromClub] = useState<number>(0);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const prePickedPlayerRef = useRef<PlayerDto | null>(null);
    const [showAllPlayers, setShowAllPlayers] = useState<boolean>(false);
    const [selectedManager, setSelectedManager] = useState<string | undefined>(undefined);
    const [searchQuery, setSearchQuery] = useState<string>('');
    const [selectedClubs, setSelectedClubs] = useState<{ value: string; label: string }[]>([]);
    const [selectedPositions, setSelectedPositions] = useState<{ value: string; label: string }[]>([]);
    const [prePickedPlayer, setPrePickedPlayer] = useState<PlayerDto | null>(null);
    const [countdownTime, setCountdownTime] = useState<number | null>(null);
    const [countdownActive, setCountdownActive] = useState<boolean>(true);
    const [hasJoinedDraft, setHasJoinedDraft] = useState(false);
    const [isDraftStartingSoon, setIsDraftStartingSoon] = useState(false);
    const [leagueStatus, setLeagueStatus] = useState<string>('');
    const [draftStartTime, setDraftStartTime] = useState<string>('');
    const [leagueAdmin, setLeagueAdmin] = useState<ManagerDto>({ id: 0, username: '' });
    const [draftCompleted, setDraftCompleted] = useState(false);
    const [draftStartTimeInput, setDraftStartTimeInput] = useState<string>('');
    const [turnDuration, setTurnDuration] = useState<number>(0);

    useEffect(() => {
        if (!league_id) {
            console.error('League ID is undefined');
            return;
        }

        fetchDraftInfo();

        return () => {
            if (clientRef.current && clientRef.current.connected) {
                clientRef.current.deactivate();
            }
            stopTimer();
        };
    }, [league_id]);

    useEffect(() => {
        if (draftStartTime) {
            const now = new Date();
            const draftStart = new Date(draftStartTime);
            const timeDifference = draftStart.getTime() - now.getTime();
            setIsDraftStartingSoon(timeDifference <= 5 * 60 * 1000);
        }
    }, [draftStartTime]);

    useEffect(() => {
        return () => {
            if (timerRef.current !== null) {
                clearInterval(timerRef.current);
            }
            stopTimer();
            if (clientRef.current && clientRef.current.active) {
                clientRef.current.deactivate();
            }
        };
    }, []);

    const connectWebSocket = () => {

        fetchLeagueData();

        const client = new Client({
            brokerURL: '/ws',
            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },
        });

        const handleDraftStateUpdate = (receivedData: ReceivedData) => {
            const selectedPlayersMap = new Map<ManagerDto, PlayerDto[]>();

            Object.keys(receivedData.selectedPlayers).forEach((key) => {
                const match = key.match(/ManagerDto\(id=(\d+), username=(\w+)\)/);
                if (match) {
                    const manager = {
                        id: parseInt(match[1]),
                        username: match[2],
                    };
                    const players = receivedData.selectedPlayers[key];
                    selectedPlayersMap.set(manager, players);
                }
            });

            const newDraftState = {
                currentManager: receivedData.currentManager,
                nextManager: receivedData.nextManager,
                selectedPlayers: selectedPlayersMap,
                draftPool: receivedData.draftPool,
                lastPickMessage: receivedData.lastPickMessage,
                remainingTime: receivedData.remainingTime,
            };

            setDraftState(newDraftState);

            if (newDraftState.currentManager.username === username && prePickedPlayerRef.current) {
                pickPlayer(prePickedPlayerRef.current.id);
                prePickedPlayerRef.current = null;
            }
        };

        client.onConnect = () => {
            client.subscribe(`/topic/draft/${league_id}`, (message) => {
                const receivedData = JSON.parse(message.body);
                setCountdownActive(false);
                handleDraftStateUpdate(receivedData);
            });

            client.subscribe(`/user/topic/draft/${league_id}/start`, (message) => {
                const millisecondsToDraftStart = JSON.parse(message.body);
                startDraftCountdown(millisecondsToDraftStart);
            });

            client.subscribe(`/user/topic/draft/${league_id}`, (message) => {
                const receivedData = JSON.parse(message.body);
                setCountdownActive(false);
                handleDraftStateUpdate(receivedData);
            });

            client.subscribe(`/topic/draft/${league_id}/complete`, () => {
                stopTimer();
                setDraftCompleted(true);
                if (clientRef.current && clientRef.current.connected) {
                    clientRef.current.deactivate();
                }
            });

            client.publish({
                destination: '/app/getDraftState',
                body: league_id,
            });
        };

        client.activate();
        clientRef.current = client;
        setHasJoinedDraft(true);
    };

    const fetchDraftInfo = async () => {
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                navigate('/login');
                return;
            }

            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

            const response = await axios.get(`/api/league/draft_info/${league_id}`);
            if (response.status === 200) {
                const { leagueStatus, draftStartTime, leagueAdmin } = response.data;
                setLeagueStatus(leagueStatus);
                setDraftStartTime(draftStartTime);
                setLeagueAdmin(leagueAdmin);
            } else {
                setError('Failed to fetch draft info');
            }
        } catch (error) {
            setError('Failed to fetch draft info');
        } finally {
            setLoading(false);
        }
    };

    const fetchLeagueData = async () => {
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                navigate('/login');
                return;
            }

            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

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

    useEffect(() => {
        if (draftState.remainingTime > 0) {
            startTimer(draftState.remainingTime);
        } else {
            stopTimer();
        }
    }, [draftState]);

    const startTimer = (time: number) => {
        if (timerRef.current !== null) {
            clearInterval(timerRef.current);
        }

        setRemainingTime(time);

        const interval = window.setInterval(() => {
            setRemainingTime((prevTime) => {
                if (prevTime <= 1000) {
                    clearInterval(interval);
                    return 0;
                }
                return prevTime - 1000;
            });
        }, 1000);

        timerRef.current = interval;
    };

    const stopTimer = () => {
        setRemainingTime(0);
        if (timerRef.current !== null) {
            clearInterval(timerRef.current);
            timerRef.current = null;
        }
    };

    const getKeyForPlayer = (playerId: number) => `player_${playerId}`;
    const getKeyForManager = (managerId: number) => `manager_${managerId}`;

    const pickPlayer = (playerId: number) => {
        if (!league_id) {
            console.error("League ID is undefined");
            return;
        }

        if (clientRef.current && clientRef.current.connected) {
            const playerPickRequest = {
                leagueId: parseInt(league_id),
                playerId: playerId
            };
            clientRef.current.publish({
                destination: '/app/pickPlayer',
                body: JSON.stringify(playerPickRequest)
            });
        } else {
            console.error('WebSocket client is not connected');
        }
    };

    useEffect(() => {
        filterValidPlayers();
    }, [draftState.selectedPlayers, draftState.draftPool]);

    const filterValidPlayers = () => {
        const currentUserSelectedPlayers = Array.from(draftState.selectedPlayers.entries())
            .find(([manager, _]) => manager.username === username)?.[1] || [];

        const goalkeepers = currentUserSelectedPlayers.filter(p => p.position === 'goalkeeper').length;
        const defenders = currentUserSelectedPlayers.filter(p => p.position === 'defender').length;
        const midfielders = currentUserSelectedPlayers.filter(p => p.position === 'midfielder').length;
        const forwards = currentUserSelectedPlayers.filter(p => p.position === 'forward').length;

        let validPositions = new Set<string>();
        if (goalkeepers < 2) {
            validPositions.add("goalkeeper");
        }
        if (defenders < 5) {
            validPositions.add("defender");
        }
        if (midfielders < 5) {
            validPositions.add("midfielder");
        }
        if (forwards < 3) {
            validPositions.add("forward");
        }

        const excludedClubs = new Set<string>();
        currentUserSelectedPlayers.forEach(player => {
            const clubName = player.clubName;
            const count = currentUserSelectedPlayers.filter(p => p.clubName === clubName).length;
            if (count >= maxPlayersFromClub) {
                excludedClubs.add(clubName);
            }
        });

        const filteredPlayers = draftState.draftPool.filter(player => {
            return !excludedClubs.has(player.clubName) && validPositions.has(player.position);
        });

        setValidPlayers(filteredPlayers);
    };

    const handlePrePick = (player: PlayerDto) => {
        setPrePickedPlayer(player);
        prePickedPlayerRef.current = player;
    };

    const handleCheckboxChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setShowAllPlayers(event.target.checked);
    };

    const handleClubChange = (selectedOptions: MultiValue<{ value: string; label: string }>) => {
        setSelectedClubs(selectedOptions as { value: string; label: string }[]);
    };

    const handlePositionChange = (selectedOptions: MultiValue<{ value: string; label: string }>) => {
        setSelectedPositions(selectedOptions as { value: string; label: string }[]);
    };

    const renderManagerTabs = () => {
        if (!draftState || !draftState.selectedPlayers) {
            return null;
        }

        return (
            <Tabs
                id="manager-tabs"
                activeKey={selectedManager}
                onSelect={(key: string | null) => key && setSelectedManager(key)}
            >
                {[...draftState.selectedPlayers.keys()].map((manager) => (
                    <Tab
                        key={getKeyForManager(manager.id)}
                        eventKey={manager.id}
                        title={manager.username}
                    >
                        <div style={{ flexGrow: 1, overflow: 'auto' }}>
                            <Table striped hover>
                                <thead>
                                    <tr>
                                        <th style={{ width: '250px' }}>Name</th>
                                        <th style={{ width: '150px' }}>Position</th>
                                        <th>Club</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {draftState.selectedPlayers.get(manager)?.map((player) => (
                                        <tr key={getKeyForPlayer(player.id)}>
                                            <td style={smallTextStyle}>{`${player.firstName || ''} ${player.lastName}`}</td>
                                            <td style={smallTextStyle}>{getPlayerPositionAbbreviation(player.position)}</td>
                                            <td style={smallTextStyle}>{player.clubName}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    </Tab>
                ))}
            </Tabs>
        );
    };

    const getFilteredPlayers = (filteredPlayers: PlayerDto[]) => {

        if (selectedClubs.length > 0) {
            const selectedClubValues = selectedClubs.map(option => option.value);
            filteredPlayers = filteredPlayers.filter(player => selectedClubValues.includes(player.clubName));
        }

        if (selectedPositions.length > 0) {
            const selectedPositionValues = selectedPositions.map(option => option.value);
            filteredPlayers = filteredPlayers.filter(player => selectedPositionValues.includes(player.position));
        }

        if (searchQuery) {
            const normalizedQuery = searchQuery.trim().toLowerCase();
            filteredPlayers = filteredPlayers.filter(player => {
                const fullName = `${player.firstName.toLowerCase()} ${player.lastName.toLowerCase()}`;
                return (
                    player.firstName.toLowerCase().startsWith(normalizedQuery) ||
                    player.lastName.toLowerCase().startsWith(normalizedQuery) ||
                    fullName.startsWith(normalizedQuery)
                );
            });
        }

        return filteredPlayers;
    };

    const renderDraftPoolTable = () => {
        return (
            <div style={{ flexGrow: 1, overflow: 'auto' }}>
                <Table striped responsive hover>
                    <thead>
                        <tr>
                            <th style={{ width: '300px' }}>Name</th>
                            <th style={{ width: '150px' }}>Position</th>
                            <th style={{ width: '150px' }}>Club</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {getFilteredPlayers(validPlayers).map((player) => (
                            <tr key={getKeyForPlayer(player.id)}>
                                <td style={{ ...smallTextStyle, verticalAlign: 'middle' }}>{`${player.firstName || ''} ${player.lastName}`}</td>
                                <td style={{ ...smallTextStyle, verticalAlign: 'middle' }}>{getPlayerPositionAbbreviation(player.position)}</td>
                                <td style={{ ...smallTextStyle, verticalAlign: 'middle' }}>{player.clubName}</td>
                                <td style={smallTextStyle}>
                                    {draftState.currentManager.username === username ? (
                                        <Button variant="primary" onClick={() => pickPlayer(player.id)} style={smallTextStyle}>Pick</Button>
                                    ) : (
                                        <Button
                                            variant={prePickedPlayer?.id === player.id ? 'warning' : 'secondary'}
                                            onClick={() => handlePrePick(player)}
                                            style={smallTextStyle}
                                        >
                                            Pick Next
                                        </Button>
                                    )}
                                </td>
                            </tr>
                        ))}
                        {showAllPlayers &&
                            getFilteredPlayers(draftState.draftPool
                                .filter(player => !validPlayers.includes(player)))
                                .map((player) => (
                                    <tr key={getKeyForPlayer(player.id)} style={{ color: 'gray' }}>
                                        <td style={smallTextStyle}>{`${player.firstName || ''} ${player.lastName}`}</td>
                                        <td style={smallTextStyle}>{getPlayerPositionAbbreviation(player.position)}</td>
                                        <td style={smallTextStyle}>{player.clubName}</td>
                                        <td></td>
                                    </tr>
                                ))}
                    </tbody>
                </Table>
            </div>
        );
    };

    const getPlayerPositionAbbreviation = (position: string) => {
        switch (position) {
            case 'goalkeeper':
                return 'GK';
            case 'defender':
                return 'DEF';
            case 'midfielder':
                return 'MID';
            case 'forward':
                return 'FWD';
            default:
                return '';
        }
    };

    const startDraftCountdown = (millisecondsToDraftStart: number) => {
        setCountdownTime(millisecondsToDraftStart);
        setCountdownActive(true);

        const interval = setInterval(() => {
            setCountdownTime((prevTime) => {
                if (prevTime === null || prevTime <= 1000) {
                    clearInterval(interval);
                    return 0;
                }
                return prevTime - 1000;
            });
        }, 1000);
    };

    const renderCountdown = () => {
        if (!countdownActive || countdownTime === null) return null;

        const minutes = Math.floor(countdownTime / 60000);
        const seconds = Math.floor((countdownTime % 60000) / 1000);

        return (
            <div>
                <h5 style={{ fontWeight: 'normal' }}>Time to draft start:</h5>
                <h5 style={{ fontWeight: 'normal' }}>{`${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`}</h5>
            </div>
        );
    };

    const draftContent = <>
        {countdownActive ? (
            renderCountdown()
        ) : (
            <>
                <Row>
                    <Col>
                        <h5 style={{ fontWeight: 'normal' }}>Current Turn: {draftState.currentManager.username}</h5>
                        <h6 style={{ fontWeight: 'normal' }}>Next Turn: {draftState.nextManager.username}</h6>
                        <h6 style={{ fontWeight: 'normal' }}>Last Pick: {draftState.lastPickMessage}</h6>
                    </Col>
                    <Col className="text-end">
                        <h5 style={{ fontWeight: 'normal' }}>Time Remaining: {Math.ceil(remainingTime / 1000)} seconds</h5>
                    </Col>
                </Row>
                <Row className='d-flex flex-column flex-grow-1' style={{ overflow: 'auto', marginTop: '20px' }}>
                    <Col className="d-flex flex-column" style={{ overflow: 'auto', maxHeight: '100%' }} sm={5}>
                        <h5 style={{ fontWeight: 'normal' }}>Selected Players</h5>
                        {renderManagerTabs()}
                    </Col>
                    <Col className="d-flex flex-column" style={{ overflow: 'auto', maxHeight: '100%' }} sm={7}>
                        <h5 style={{ fontWeight: 'normal' }}>Draft Pool</h5>
                        <Form.Control
                            type="text"
                            placeholder="Search by name"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                        />

                        <Row className='mt-1'>
                            <Col sm={5}>
                                <Form.Group>
                                    <Select
                                        closeMenuOnSelect={false}
                                        isMulti
                                        placeholder="Filter by position"
                                        value={selectedPositions}
                                        onChange={handlePositionChange}
                                        options={[
                                            { value: 'goalkeeper', label: 'Goalkeeper' },
                                            { value: 'defender', label: 'Defender' },
                                            { value: 'midfielder', label: 'Midfielder' },
                                            { value: 'forward', label: 'Forward' }
                                        ]}
                                        styles={customReactSelectStyles}
                                    />
                                </Form.Group>
                            </Col>
                            <Col sm={7}>
                                <Form.Group>
                                    <Select
                                        closeMenuOnSelect={false}
                                        isMulti
                                        placeholder="Filter by club"
                                        value={selectedClubs}
                                        onChange={handleClubChange}
                                        options={Array.from(new Set(draftState.draftPool.map(player => player.clubName))).map((clubName) => ({
                                            value: clubName,
                                            label: clubName
                                        }))}
                                        styles={customReactSelectStyles}
                                    />
                                </Form.Group>
                            </Col>
                        </Row>
                        <Form.Check className='mt-1'
                            type="checkbox"
                            label="Show invalid players"
                            checked={showAllPlayers}
                            onChange={handleCheckboxChange}
                        />
                        {renderDraftPoolTable()}
                    </Col>
                </Row>
            </>
        )}
    </>

    function formatDate(date: Date): string {
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const year = date.getFullYear();

        return `${hours}:${minutes} on ${day}/${month}/${year}`;
    }

    const renderDraftContent = () => {
        if (!leagueStatus) return null;

        switch (leagueStatus) {
            case "drafting":
                return (
                    <div className='d-flex flex-column flex-grow-1' style={{ overflow: 'hidden' }}>
                        {!hasJoinedDraft &&
                            <div>
                                <h5 style={{ fontWeight: 'normal' }}>Draft has started.</h5>
                                <Button variant="primary" onClick={connectWebSocket}>Join Draft</Button>
                            </div>
                        }
                        {hasJoinedDraft && (
                            draftContent
                        )}
                    </div>
                );
            case "waiting for draft":
                return (
                    <div className='d-flex flex-column flex-grow-1' style={{ overflow: 'hidden' }}>
                        {!hasJoinedDraft &&
                            <div>
                                <h5 style={{ fontWeight: 'normal' }}>
                                    Draft will start at {draftStartTime ? formatDate(new Date(draftStartTime)) : 'Not specified'}.
                                </h5>
                                {isDraftStartingSoon && <Button variant="primary" onClick={connectWebSocket}>Join Draft</Button>}
                            </div>
                        }
                        {hasJoinedDraft && (
                            draftContent
                        )}
                    </div>
                );
            case "created":
                return (
                    <div className='d-flex flex-column flex-grow-1' style={{ overflow: 'hidden' }}>
                        <Row className="mb-3">
                            <Col>
                                <h5 style={{ fontWeight: 'normal' }}>Draft has not been scheduled.</h5>
                            </Col>
                        </Row>
                        {leagueAdmin.username === username && (
                            <Row className="justify-content-center">
                                <Col md={4}>
                                    <h5 style={{ fontWeight: 'normal' }} className="mb-3">Submit draft settings.</h5>
                                    <Form onSubmit={handleSubmitDraftSettings}>
                                        <Form.Group controlId="formDraftStartTime" className="mb-3">
                                            <Form.Label>Draft start time</Form.Label>
                                            <Form.Control
                                                type="datetime-local"
                                                value={draftStartTimeInput}
                                                onChange={(e) => setDraftStartTimeInput(e.target.value)}
                                                required
                                            />
                                        </Form.Group>
                                        <Form.Group controlId="formTurnDuration" className="mb-3">
                                            <Form.Label>Turn duration (seconds)</Form.Label>
                                            <Form.Control
                                                type="number"
                                                value={turnDuration}
                                                onChange={(e) => setTurnDuration(Number(e.target.value))}
                                                required
                                            />
                                        </Form.Group>
                                        <Button variant="primary" type="submit">
                                            Submit
                                        </Button>
                                    </Form>
                                </Col>
                            </Row>
                        )}
                    </div>
                );
            default:
                return <h5 style={{ fontWeight: 'normal' }}>You cannot access this.</h5>;
        }
    };

    const handleSubmitDraftSettings = async (event: React.FormEvent) => {
        event.preventDefault();
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                navigate('/login');
                return;
            }

            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

            const draftStartTime = new Date(draftStartTimeInput).toISOString();

            const response = await axios.post(`/api/league/submit_draft_settings/${league_id}`, {
                draftStartTime,
                turnDuration
            });

            if (response.status === 200) {
                fetchDraftInfo();
                setError(null);
            } else {
                setError('Failed to submit draft settings');
            }
        } catch (error: any) {
            handleFetchError(error);
        }
    };

    const renderLoadingAndError = () => {
        if (loading) {
            return <div className="text-center">
                <Spinner animation="border" role="status">
                    <span className="visually-hidden">Loading...</span>
                </Spinner>
                <p>Loading...</p>
            </div>;
        }
        if (error) {
            return <div className="alert alert-danger">{error}</div>;
        }
        return null;
    };

    const renderDraftCompletedModal = () => {
        return (
            <Modal show={draftCompleted} onHide={handleCloseModal} centered>
                <Modal.Header closeButton>
                    <Modal.Title>Draft Completed!</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p>The draft has been completed.</p>
                </Modal.Body>
            </Modal>
        );
    };

    const handleCloseModal = () => {
        setDraftCompleted(false);
    };

    return (
        <div className="flex-grow-1 d-flex flex-column p-5" style={{ overflow: 'auto' }}>
            <h2 style={{ marginBottom: '20px' }}>Draft</h2>
            {renderLoadingAndError()}
            {renderDraftContent()}
            {renderDraftCompletedModal()}
        </div>
    );
};

export default DraftPage;
