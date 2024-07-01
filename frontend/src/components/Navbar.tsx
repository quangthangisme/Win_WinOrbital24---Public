import React from 'react';
import { Navbar, Nav, NavDropdown, Button, Container } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const NavbarComponent: React.FC = () => {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');

    const handleLogout = async () => {
        try {
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            navigate('/login');
        } catch (error) {
            console.error('Logout failed', error);
        }
    };

    const handleLogin = () => {
        navigate('/login');
    };

    return (
        <Navbar bg="dark" variant="dark" expand="lg" className="sticky-top">
            <Container>
                <Navbar.Brand onClick={() => navigate('/')} style={{ cursor: 'pointer', color: '#FFFFFF' }}>NUSFF</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                        <NavDropdown title="League" id="basic-nav-dropdown">
                            <NavDropdown.Item as={Button} onClick={() => navigate('/myleagues')}>My Leagues</NavDropdown.Item>
                            <NavDropdown.Item as={Button} onClick={() => navigate('/create_league')}>Create a League</NavDropdown.Item>
                            <NavDropdown.Item as={Button} onClick={() => navigate('/join_league')}>Join a League</NavDropdown.Item>
                        </NavDropdown>
                    </Nav>
                    {token ? (
                        <>
                            <Navbar.Text className="me-5">{username}</Navbar.Text>
                            <Button variant="outline-light" onClick={handleLogout}>Log Out</Button>
                        </>
                    ) : (
                        <Button variant="outline-light" onClick={handleLogin}>Log In</Button>
                    )}
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default NavbarComponent;