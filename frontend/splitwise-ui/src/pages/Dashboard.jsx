import React from 'react'
import NavBar from '../components/NavBar'
import Summary from '../components/Summary';
import GroupsAndActivity from '../components/GroupsAndActivity';

const Dashboard = () => {
  return (
    <div className='dashboard-container'>
        <NavBar/>
        <main>
            <Summary/>
            <GroupsAndActivity/>
        </main>
    </div>
  )
}

export default Dashboard
