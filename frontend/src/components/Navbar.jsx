import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Calendar, UserCheck, Shield, LogOut, ArrowRightLeft } from 'lucide-react';

const Navbar = () => {
  const { user, logout } = useAuth();
  const location = useLocation();

  if (!user) return null;

  return (
    <nav style={{
      backgroundColor: '#ffffff',
      borderBottom: '1px solid var(--border-color)',
      padding: '0.75rem 2rem',
      position: 'sticky',
      top: 0,
      zIndex: 500,
      boxShadow: 'var(--shadow-sm)'
    }}>
      <div style={{ maxWidth: '1280px', margin: '0 auto', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        
        {/* Brand Logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{
            width: '38px', height: '38px', borderRadius: 'var(--radius-md)',
            background: 'linear-gradient(135deg, #84cc16, #4d7c0f)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            color: '#ffffff', boxShadow: '0 2px 8px rgba(132, 204, 22, 0.4)'
          }}>
            <Calendar size={20} />
          </div>
          <div>
            <div style={{ fontWeight: 800, fontSize: '1.1rem', letterSpacing: '-0.02em', color: '#0f172a' }}>
              LeaveFlow <span style={{ 
                background: '#84cc16', color: '#0f172a', padding: '0.1rem 0.4rem', 
                borderRadius: '4px', fontSize: '0.8rem', fontWeight: 800 
              }}>HQ</span>
            </div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Approval Chains & Escalations</div>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <Link
            to="/employee"
            className={`btn ${location.pathname === '/employee' ? 'btn-primary' : 'btn-outline'}`}
            style={{ 
              padding: '0.5rem 0.9rem', 
              fontSize: '0.85rem',
              ...(location.pathname === '/employee' ? { backgroundColor: '#0f172a', color: '#84cc16' } : {})
            }}
          >
            <Calendar size={15} /> My Leave Workspace
          </Link>

          <Link
            to="/swap-marketplace"
            className={`btn ${location.pathname === '/swap-marketplace' ? 'btn-primary' : 'btn-outline'}`}
            style={{ 
              padding: '0.5rem 0.9rem', 
              fontSize: '0.85rem',
              ...(location.pathname === '/swap-marketplace' ? { backgroundColor: '#0f172a', color: '#84cc16' } : {})
            }}
          >
            <ArrowRightLeft size={15} /> Swap Market
          </Link>

          {(user.role === 'MANAGER' || user.role === 'HR') && (
            <Link
              to="/manager"
              className={`btn ${location.pathname === '/manager' ? 'btn-primary' : 'btn-outline'}`}
              style={{ 
                padding: '0.5rem 0.9rem', 
                fontSize: '0.85rem',
                ...(location.pathname === '/manager' ? { backgroundColor: '#0f172a', color: '#84cc16' } : {})
              }}
            >
              <UserCheck size={15} /> Team Manager
            </Link>
          )}

          {user.role === 'HR' && (
            <Link
              to="/hr"
              className={`btn ${location.pathname === '/hr' ? 'btn-primary' : 'btn-outline'}`}
              style={{ 
                padding: '0.5rem 0.9rem', 
                fontSize: '0.85rem',
                ...(location.pathname === '/hr' ? { backgroundColor: '#0f172a', color: '#84cc16' } : {})
              }}
            >
              <Shield size={15} /> HR Control Center
            </Link>
          )}
        </div>

        {/* User Info & Actions */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>

          {/* User Badge */}
          <div style={{
            display: 'flex', alignItems: 'center', gap: '0.65rem',
            padding: '0.35rem 0.75rem', borderRadius: '9999px',
            background: '#f8fafc', border: '1px solid var(--border-color)'
          }}>
            <div style={{
              width: '28px', height: '28px', borderRadius: '50%',
              background: '#ecfccb', color: '#3f6212',
              display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, fontSize: '0.8rem'
            }}>
              {user.name ? user.name.charAt(0) : 'U'}
            </div>
            <div style={{ lineHeight: 1.2 }}>
              <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#0f172a' }}>{user.name}</div>
              <div style={{ fontSize: '0.68rem', color: '#65a30d', fontWeight: 700 }}>{user.role}</div>
            </div>
          </div>

          <button
            onClick={logout}
            className="btn btn-outline"
            style={{ padding: '0.45rem', borderRadius: '50%', color: 'var(--text-muted)' }}
            title="Logout"
          >
            <LogOut size={16} />
          </button>
        </div>

      </div>
    </nav>
  );
};

export default Navbar;
