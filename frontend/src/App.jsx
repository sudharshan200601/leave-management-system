import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import EmployeeWorkspace from './pages/EmployeeWorkspace';
import ManagerDashboard from './pages/ManagerDashboard';
import HrCenter from './pages/HrCenter';
import LeaveSwapMarketplace from './pages/LeaveSwapMarketplace';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
          <Navbar />
          <main style={{ flex: 1 }}>
            <Routes>
              <Route path="/login" element={<Login />} />
              
              <Route element={<ProtectedRoute allowedRoles={['EMPLOYEE', 'MANAGER', 'HR']} />}>
                <Route path="/employee" element={<EmployeeWorkspace />} />
                <Route path="/swap-marketplace" element={<LeaveSwapMarketplace />} />
              </Route>

              <Route element={<ProtectedRoute allowedRoles={['MANAGER', 'HR']} />}>
                <Route path="/manager" element={<ManagerDashboard />} />
              </Route>

              <Route element={<ProtectedRoute allowedRoles={['HR']} />}>
                <Route path="/hr" element={<HrCenter />} />
              </Route>

              <Route path="*" element={<Navigate to="/employee" replace />} />
            </Routes>
          </main>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
