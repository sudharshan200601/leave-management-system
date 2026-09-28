import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Calendar, Shield, UserCheck, Users, Lock, Mail, ArrowRight } from 'lucide-react';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await login(email, password);
      if (data.role === 'HR') navigate('/hr');
      else if (data.role === 'MANAGER') navigate('/manager');
      else navigate('/employee');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      padding: '2rem', background: 'radial-gradient(circle at 50% 30%, rgba(132, 204, 22, 0.12), transparent 70%), #f8fafc'
    }}>
      <div style={{ width: '100%', maxWidth: '450px', display: 'flex', flexDirection: 'column', gap: '2rem' }}>
        
        {/* Login Form */}
        <div className="glass-panel" style={{ padding: '2.5rem', boxShadow: 'var(--shadow-lg)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
            <div style={{
              width: '44px', height: '44px', borderRadius: 'var(--radius-md)',
              background: 'linear-gradient(135deg, #84cc16, #4d7c0f)',
              display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#ffffff',
              boxShadow: '0 2px 8px rgba(132, 204, 22, 0.4)'
            }}>
              <Calendar size={24} />
            </div>
            <div>
              <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#0f172a' }}>Welcome Back</h2>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Sign in to LeaveFlow HQ</p>
            </div>
          </div>

          {error && (
            <div style={{
              backgroundColor: 'var(--danger-bg)', color: 'var(--danger-color)',
              border: '1px solid rgba(239, 68, 68, 0.3)', padding: '0.75rem 1rem',
              borderRadius: 'var(--radius-md)', fontSize: '0.85rem', marginBottom: '1.25rem'
            }}>
              {error}
            </div>
          )}

          <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.4rem', color: 'var(--text-secondary)' }}>
                Email Address
              </label>
              <div style={{ position: 'relative' }}>
                <Mail size={18} style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  style={{ paddingLeft: '2.75rem' }}
                  placeholder="Enter your email"
                  required
                />
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.4rem', color: 'var(--text-secondary)' }}>
                Password
              </label>
              <div style={{ position: 'relative' }}>
                <Lock size={18} style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  style={{ paddingLeft: '2.75rem' }}
                  placeholder="Enter your password"
                  required
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary" disabled={loading} style={{ width: '100%', padding: '0.85rem', marginTop: '0.5rem' }}>
              {loading ? 'Authenticating...' : 'Sign In to Dashboard'} <ArrowRight size={18} />
            </button>
          </form>
        </div>

      </div>
    </div>
  );
};

export default Login;
