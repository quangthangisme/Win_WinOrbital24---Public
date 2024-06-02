import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import LoginForm from './components/LoginForm';
import RegistrationForm from './components/RegistrationForm';
import MyLeaguesPage from './components/MyLeaguesPage';
import CreateLeaguePage from './components/CreateLeaguePage';
import JoinLeaguePage from './components/JoinLeaguePage';
import LeagueTeamsPage from './components/LeagueTeamsPage';

const App = () => {
  return (
    <Router>
      <div className="vh-100">
        <Routes>
          <Route path="/" element={<MyLeaguesPage/>} />
          <Route path="/login" element={<LoginForm/>} />
          <Route path="/registration" element={<RegistrationForm/>} />
          <Route path="/myleagues" element={<MyLeaguesPage/>} />
          <Route path="/create_league" element={<CreateLeaguePage/>} />
          <Route path="/join_league" element={<JoinLeaguePage/>} />
          <Route path="/league/:league_id" element={<LeagueTeamsPage />} />
        </Routes>
      </div>
    </Router>
  );
};

export default App;