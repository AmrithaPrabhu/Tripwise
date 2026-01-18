import { useState } from 'react'
import './App.css'
import {BrowserRouter, Route, Routes} from 'react-router-dom'
import SignUp from './pages/SignUp'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import GroupPage from './pages/GroupPage'
import ProtectedRoute from './utils/ProtectedRoute'

function App() {
  const [count, setCount] = useState(0)

  return (
    
       <Routes>
        <Route path='/signup' element={<SignUp/>}/>
          <Route path='/' element=
          {<ProtectedRoute><Dashboard/></ProtectedRoute>}/>
          <Route path='/login' element={<Login/>}/>
          <Route path="/groups/:groupId" element={<ProtectedRoute><GroupPage /></ProtectedRoute>} />

       </Routes>    
  )
}

export default App
