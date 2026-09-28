import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ allowedRoles }) => {
  const { user, token } = useAuth();

  if (!token || !user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    // If user role is not permitted for this route, redirect to their home workspace
    if (user.role === 'HR') return <Navigate to="/hr" replace />;
    if (user.role === 'MANAGER') return <Navigate to="/manager" replace />;
    return <Navigate to="/employee" replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
