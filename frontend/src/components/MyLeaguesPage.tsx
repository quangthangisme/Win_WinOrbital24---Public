import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

interface League {
  id: number;
  name: string;
  code: string;
}

const MyLeaguesPage: React.FC = () => {
  const navigate = useNavigate();
  const [leagues, setLeagues] = useState<League[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchLeagues = async () => {
      try {
        const isAuthenticatedResponse = await axios.get('/api/authenticated');
        if (isAuthenticatedResponse.status != 200) {
          navigate('/login');
          return;
        }

        const leaguesResponse = await axios.get('/api/manager/myleagues');
        if (leaguesResponse.status != 200) {
          const errorMessage = await leaguesResponse.data;
          setError(errorMessage || 'Failed to fetch leagues');
          return;
        }

        setLeagues(leaguesResponse.data);
      } catch (error) {
        setError('An unexpected error occurred');
      }
    };

    fetchLeagues();
  }, [navigate]);

  const handleLogout = async () => {
    try {
      const response = await axios.get('/api/perform_logout');
      if (response.status === 200) {
        navigate('/login');
      }
    } catch (error) {
      console.error('Failed to logout:', error);
    }
  };

  return (
    <div className="container">
      <div className="my-leagues-container mt-5">
        <div className="d-flex justify-content-between align-items-center mb-4">
          <h2 className="mb-0">My Leagues</h2>
          <button onClick={handleLogout} className="btn btn-danger">Logout</button>
        </div>
        {error ? (
          <p className="error-message text-center">Error: {error}</p>
        ) : (
          <div>
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Code</th>
                </tr>
              </thead>
              <tbody>
                {leagues.map((league) => (
                  <tr key={league.id}>
                    <td><Link to={`/league/${league.id}`}>{league.name}</Link></td>
                    <td>{league.code}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="d-flex justify-content-center">
              <Link to="/create_league" className="btn btn-primary me-3">Create League</Link>
              <Link to="/join_league" className="btn btn-secondary">Join League</Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyLeaguesPage;
