import React from 'react';
import { Accordion } from 'react-bootstrap';
import '../assets/css/Accordion.css'

const HowToPlayPage: React.FC = () => {
    return <div className="how-to-play-page">
        <h2>How to Play</h2>

        <Accordion defaultActiveKey={[]} alwaysOpen>
            <Accordion.Item eventKey="0">
                <Accordion.Header><h4>Assemble your league</h4></Accordion.Header>
                <Accordion.Body>
                    <p>
                        Each league will be set up by one user, who will acts as the league admin.
                        The league's admin can customize the league when creating the league such as setting its scoring rule.
                        Once the league is created, the admin is provided with a code they can share with other users for them to join the league.
                    </p>
                    <p>
                        After creating the league, the admin must submit a time and date to conduct the league draft.
                    </p>
                    <p>
                        All leagues are private and only members of the league are able to view the league's information.
                        A maximum of 16 users can join a league.
                    </p>
                </Accordion.Body>
            </Accordion.Item>
            <Accordion.Item eventKey="1">
                <Accordion.Header><h4>Draft your squad</h4></Accordion.Header>
                <Accordion.Body>
                    <p>
                        The draft start time will be set by the league's admin.
                    </p>
                    <p>
                        Each manager takes turns to pick a player until their squad is complete.
                        The order in which managers is in the snake pattern.
                        For example, if a league has 4 managers, the draft picks would proceed as follows: M1, M2, M3, M4, M4, M3, M2, M1, M1, M2, M3 and so on.
                    </p>
                    <p>
                        The draft ends when all teams have a valid squad of 15 players consisting of 2 goalkeeper, 5 defenders, 5 midfielders, and 3 forwards.
                    </p>
                    <p>
                        If you do not select a player within the allocated time limit, a random valid player will be assigned to you.
                    </p>
                </Accordion.Body>
            </Accordion.Item>
            <Accordion.Item eventKey="2">
                <Accordion.Header><h4>Manage your team</h4></Accordion.Header>
                <Accordion.Body>
                    <p>
                        The draft start time will be set by the league's admin.
                    </p>
                    <p>
                        Each manager takes turns to pick a player until their squad is complete.
                        The order in which managers is in the snake pattern.
                        For example, if a league has 4 managers, the draft picks would proceed as follows: M1, M2, M3, M4, M4, M3, M2, M1, M1, M2, M3 and so on.
                    </p>
                    <p>
                        The draft ends when all teams have a valid squad of 15 players consisting of 2 goalkeeper, 5 defenders, 5 midfielders, and 3 forwards.
                    </p>
                    <p>
                        If you do not select a player within the allocated time limit, a random valid player will be assigned to you.
                    </p>
                </Accordion.Body>
            </Accordion.Item>
        </Accordion>
    </div>
};

export default HowToPlayPage;
