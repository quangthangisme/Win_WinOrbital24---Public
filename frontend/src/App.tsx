import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import LoginForm from './components/LoginForm';
import RegistrationForm from './components/RegistrationForm';
import MyLeaguesPage from './components/MyLeaguesPage';
import CreateLeaguePage from './components/CreateLeaguePage';
import JoinLeaguePage from './components/JoinLeaguePage';
import NavbarComponent from './components/Navbar';
import LeagueComponentWithSidebar from './components/LeagueComponentWithSidebar';
import HowToPlayPage from './components/HowToPlayPage';

const App: React.FC = () => {

    return (
        <Router>
            <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
                <NavbarComponent />
                <div className="d-flex flex-grow-1" style={{ overflow: 'auto' }}>
                    <Routes>
                        <Route path="/" element={<MyLeaguesPage />} />
                        <Route path="/login" element={<LoginForm />} />
                        <Route path="/registration" element={<RegistrationForm />} />
                        <Route path="/myleagues" element={<MyLeaguesPage />} />
                        <Route path="/create_league" element={<CreateLeaguePage />} />
                        <Route path="/join_league" element={<JoinLeaguePage />} />
                        <Route path="/guide" element={<HowToPlayPage />} />
                        <Route path="/league/:league_id/*" element={<LeagueComponentWithSidebar />} />
                    </Routes>
                </div>
            </div>
        </Router>
    );
};

export default App;
