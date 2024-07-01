import React, { useEffect, useState } from 'react';
import { Routes, Route, useParams, useNavigate } from 'react-router-dom';
import SidebarComponent from './Sidebar';
import MyTeamPage from './MyTeamPage';
import LeagueScoringRulePage from './LeagueScoringRulePage';
import DraftPage from './DraftPage';
import LeagueRankingPage from './LeagueRankingPage';
import axios from 'axios';
import TeamDataPage from './TeamDataPage';

const LeagueComponentWithSidebar: React.FC = () => {
    const { league_id } = useParams<{ league_id: string }>();
    const [leagueName, setLeagueName] = useState<string>('');
    const navigate = useNavigate(); // useNavigate hook for navigation
    const token = localStorage.getItem('token'); // Fetch token from localStorage

    useEffect(() => {
        const fetchLeagueName = async () => {
            try {
                const leagueNameResponse = await axios.get(`/api/league/name/${league_id}`, {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                });
                if (leagueNameResponse.status !== 200) {
                    console.error('Failed to fetch league name');
                    return;
                }
                setLeagueName(leagueNameResponse.data);
            } catch (error) {
                console.error('Failed to fetch league name', error);
            }
        };

        if (token) {
            fetchLeagueName();
        } else {
            navigate('/login');
        }
    }, [league_id, token, navigate]);

    return (
        <div className="d-flex flex-grow-1" style={{ overflow: 'auto' }}>
            <SidebarComponent leagueName={leagueName} />
            <div className="d-flex flex-grow-1" style={{ overflow: 'auto' }}>
                <Routes>
                    <Route path="/" element={<LeagueRankingPage />} />
                    <Route path="ranking" element={<LeagueRankingPage />} />
                    <Route path="myteam" element={<MyTeamPage />} />
                    <Route path="scoring_rule" element={<LeagueScoringRulePage />} />
                    <Route path="draft" element={<DraftPage />} />
                    <Route path="team_data" element={<TeamDataPage />} />
                </Routes>
            </div>
        </div>
    );
};

export default LeagueComponentWithSidebar;
