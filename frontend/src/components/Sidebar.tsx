import React from 'react';
import { Link, useParams } from 'react-router-dom';

interface SidebarProps {
    leagueName: string;
}

const Sidebar: React.FC<SidebarProps> = ({ leagueName }) => {
    const { league_id } = useParams();

    return (
        <div className="sidebar bg-light p-3" style={{ width: "175px" }}>
            <h4>{leagueName}</h4>
            <ul className="nav flex-column">
                <li className="nav-item">
                    <Link to={`/league/${league_id}`} className="nav-link">Ranking</Link>
                </li>
                <li className="nav-item">
                    <Link to={`/league/${league_id}/team_data`} className="nav-link">Teams</Link>
                </li>
                <li className="nav-item">
                    <Link to={`/league/${league_id}/myteam`} className="nav-link">Manage</Link>
                </li>
                <li className="nav-item">
                    <Link to={`/league/${league_id}/scoring_rule`} className="nav-link">Rule</Link>
                </li>
                <li className="nav-item">
                    <Link to={`/league/${league_id}/draft`} className="nav-link">Draft</Link>
                </li>
            </ul>
        </div>
    );
};

export default Sidebar;