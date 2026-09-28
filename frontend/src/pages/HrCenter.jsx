import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import StatusBadge from '../components/StatusBadge';
import ConflictBadge from '../components/ConflictBadge';
import ApprovalTimeline from '../components/ApprovalTimeline';
import { Shield, CheckCircle2, XCircle, Calendar, Plus, Trash2, RefreshCw, Filter, AlertCircle, ChevronDown, ChevronUp, Users } from 'lucide-react';

const HrCenter = () => {
  const [activeTab, setActiveTab] = useState('pending'); // 'pending' | 'all' | 'holidays' | 'balances'
  
  // Data states
  const [pendingRequests, setPendingRequests] = useState([]);
  const [allRequests, setAllRequests] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [holidays, setHolidays] = useState([]);
  const [balances, setBalances] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);

  // New Holiday Form State
  const [newHolidayDate, setNewHolidayDate] = useState('');
  const [newHolidayName, setNewHolidayName] = useState('');
  const [addingHoliday, setAddingHoliday] = useState(false);

  // Action Modal State
  const [actionModal, setActionModal] = useState({ open: false, type: '', reqId: null, employeeName: '' });
  const [comment, setComment] = useState('');
  const [submittingAction, setSubmittingAction] = useState(false);

  // Employee Form State
  const [newEmp, setNewEmp] = useState({ name: '', email: '', plainPassword: '', role: 'EMPLOYEE', teamId: 1, managerId: '' });
  const [addingEmp, setAddingEmp] = useState(false);
  const [uploadFile, setUploadFile] = useState(null);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    fetchData();
  }, [activeTab, statusFilter]);

  const fetchData = async () => {
    setLoading(true);
    try {
      if (activeTab === 'pending') {
        const res = await api.get('/hr/pending');
        setPendingRequests(res.data);
      } else if (activeTab === 'all') {
        const url = statusFilter ? `/hr/all-requests?status=${statusFilter}` : '/hr/all-requests';
        const res = await api.get(url);
        setAllRequests(res.data);
      } else if (activeTab === 'holidays') {
        const res = await api.get('/hr/holidays');
        setHolidays(res.data);
      } else if (activeTab === 'balances') {
        const res = await api.get('/hr/balances');
        setBalances(res.data);
      } else if (activeTab === 'employees') {
        const res = await api.get('/hr/employees');
        setEmployees(res.data);
      }
    } catch (err) {
      console.error('Failed to fetch HR data', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async () => {
    if (!actionModal.reqId || !actionModal.type) return;
    setSubmittingAction(true);
    try {
      const endpoint = `/hr/${actionModal.reqId}/${actionModal.type}`;
      await api.post(endpoint, { comment });
      setActionModal({ open: false, type: '', reqId: null, employeeName: '' });
      setComment('');
      fetchData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to submit HR final decision');
    } finally {
      setSubmittingAction(false);
    }
  };

  const handleAddHoliday = async (e) => {
    e.preventDefault();
    if (!newHolidayDate || !newHolidayName) return;
    setAddingHoliday(true);
    try {
      await api.post('/hr/holidays', { date: newHolidayDate, name: newHolidayName });
      setNewHolidayDate('');
      setNewHolidayName('');
      fetchData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create holiday');
    } finally {
      setAddingHoliday(false);
    }
  };

  const handleDeleteHoliday = async (id) => {
    if (!window.confirm('Delete this company holiday?')) return;
    try {
      await api.delete(`/hr/holidays/${id}`);
      fetchData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete holiday');
    }
  };

  const handleAddEmployee = async (e) => {
    e.preventDefault();
    setAddingEmp(true);
    try {
      await api.post('/hr/employees', newEmp);
      setNewEmp({ name: '', email: '', plainPassword: '', role: 'EMPLOYEE', teamId: 1, managerId: '' });
      fetchData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to add employee');
    } finally {
      setAddingEmp(false);
    }
  };

  const handleUploadEmployees = async (e) => {
    e.preventDefault();
    if (!uploadFile) return;
    setUploading(true);
    const formData = new FormData();
    formData.append('file', uploadFile);
    try {
      await api.post('/hr/employees/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setUploadFile(null);
      fetchData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to upload file');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div style={{ maxWidth: '1280px', margin: '2rem auto', padding: '0 1.5rem' }}>
      
      {/* Header */}
      <div style={{ marginBottom: '2rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>HR Admin Control Center</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Final approval authority, auto-escalations queue, company holidays CRUD, and org balance audit.
          </p>
        </div>
        <button onClick={fetchData} className="btn btn-outline" style={{ padding: '0.5rem 0.85rem' }}>
          <RefreshCw size={14} /> Refresh
        </button>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.75rem', borderBottom: '1px solid var(--border-color)', marginBottom: '1.5rem' }}>
        <button
          onClick={() => setActiveTab('pending')}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'pending' ? '3px solid #84cc16' : '3px solid transparent',
            color: activeTab === 'pending' ? '#0f172a' : 'var(--text-muted)',
            fontWeight: 800,
            fontSize: '0.9rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Shield size={16} color={activeTab === 'pending' ? '#4d7c0f' : 'currentColor'} /> Pending HR Approvals
        </button>

        <button
          onClick={() => setActiveTab('all')}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'all' ? '3px solid #84cc16' : '3px solid transparent',
            color: activeTab === 'all' ? '#0f172a' : 'var(--text-muted)',
            fontWeight: 800,
            fontSize: '0.9rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Filter size={16} color={activeTab === 'all' ? '#4d7c0f' : 'currentColor'} /> All Org Requests
        </button>

        <button
          onClick={() => setActiveTab('holidays')}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'holidays' ? '3px solid #84cc16' : '3px solid transparent',
            color: activeTab === 'holidays' ? '#0f172a' : 'var(--text-muted)',
            fontWeight: 800,
            fontSize: '0.9rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Calendar size={16} color={activeTab === 'holidays' ? '#4d7c0f' : 'currentColor'} /> Holidays Management
        </button>

        <button
          onClick={() => setActiveTab('balances')}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'balances' ? '3px solid #84cc16' : '3px solid transparent',
            color: activeTab === 'balances' ? '#0f172a' : 'var(--text-muted)',
            fontWeight: 800,
            fontSize: '0.9rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Users size={16} color={activeTab === 'balances' ? '#4d7c0f' : 'currentColor'} /> Employee Balances Audit
        </button>

        <button
          onClick={() => setActiveTab('employees')}
          style={{
            padding: '0.75rem 1.25rem',
            background: 'none',
            border: 'none',
            borderBottom: activeTab === 'employees' ? '3px solid #84cc16' : '3px solid transparent',
            color: activeTab === 'employees' ? '#0f172a' : 'var(--text-muted)',
            fontWeight: 800,
            fontSize: '0.9rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Users size={16} color={activeTab === 'employees' ? '#4d7c0f' : 'currentColor'} /> Employee Management
        </button>
      </div>

      {/* Tab 1: Pending HR Approvals */}
      {activeTab === 'pending' && (
        <div className="glass-panel">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Shield size={18} color="var(--accent-primary)" /> Awaiting Final HR Approval
          </h3>

          {loading ? (
            <p style={{ color: 'var(--text-muted)' }}>Loading pending queue...</p>
          ) : pendingRequests.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--text-muted)' }}>
              <CheckCircle2 size={40} style={{ opacity: 0.4, color: 'var(--success-color)', marginBottom: '1rem' }} />
              <p style={{ fontWeight: 600 }}>HR Approval Queue Clear!</p>
              <p style={{ fontSize: '0.8rem', opacity: 0.7 }}>No requests currently awaiting final HR action.</p>
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Employee</th>
                    <th>Type & Reason</th>
                    <th>Dates & Days</th>
                    <th>Conflict Badge</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingRequests.map((req) => {
                    const isExpanded = expandedId === req.id;
                    return (
                      <React.Fragment key={req.id}>
                        <tr>
                          <td>
                            <div style={{ fontWeight: 700 }}>{req.employeeName}</div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{req.employeeEmail}</div>
                          </td>
                          <td>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', flexWrap: 'wrap' }}>
                              <span style={{ fontWeight: 700, color: 'var(--accent-primary)' }}>{req.leaveType}</span>
                              {req.compensatoryWorkingDate && (
                                <span style={{ fontSize: '0.72rem', color: '#854d0e', backgroundColor: '#fef9c3', border: '1px solid #fef08a', padding: '0.1rem 0.35rem', borderRadius: '4px' }}>
                                  🗓️ Weekend: {req.compensatoryWorkingDate}
                                </span>
                              )}
                              {req.odType && (
                                <span style={{ fontSize: '0.72rem', color: '#1e40af', backgroundColor: '#dbeafe', border: '1px solid #bfdbfe', padding: '0.1rem 0.35rem', borderRadius: '4px' }}>
                                  📍 {req.odType} at {req.location}
                                </span>
                              )}
                            </div>
                            <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>"{req.reason}"</div>
                            {req.remarks && (
                                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Remarks: {req.remarks}</div>
                            )}
                            {req.documentData && (
                              <div style={{ marginTop: '0.35rem' }}>
                                <button
                                  type="button"
                                  onClick={() => window.open(req.documentData, '_blank')}
                                  className="btn btn-outline"
                                  style={{ padding: '0.2rem 0.5rem', fontSize: '0.72rem', display: 'inline-flex', alignItems: 'center', gap: '0.3rem', color: '#166534', backgroundColor: '#dcfce7', borderColor: '#86efac' }}
                                >
                                  📄 View Document
                                </button>
                              </div>
                            )}
                          </td>
                          <td>
                            {req.leaveType === 'ON_DUTY' ? (
                                <>
                                  <div style={{ fontWeight: 600 }}>{req.startDate}</div>
                                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{req.startTime} - {req.endTime}</div>
                                </>
                            ) : (
                                <>
                                  <div style={{ fontWeight: 600 }}>{req.startDate} to {req.endDate}</div>
                                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{req.workingDays} working days</div>
                                </>
                            )}
                          </td>
                          <td>
                            <ConflictBadge flag={req.conflictFlag} severity={req.conflictSeverity} details={req.conflictDetails} />
                          </td>
                          <td>
                            <StatusBadge status={req.status} escalated={req.escalated} />
                          </td>
                          <td>
                            <div style={{ display: 'flex', gap: '0.35rem' }}>
                              <button
                                onClick={() => setActionModal({ open: true, type: 'approve', reqId: req.id, employeeName: req.employeeName })}
                                className="btn btn-success"
                                style={{ padding: '0.35rem 0.65rem', fontSize: '0.75rem' }}
                              >
                                <CheckCircle2 size={13} /> Final Approve
                              </button>
                              <button
                                onClick={() => setActionModal({ open: true, type: 'reject', reqId: req.id, employeeName: req.employeeName })}
                                className="btn btn-danger"
                                style={{ padding: '0.35rem 0.65rem', fontSize: '0.75rem' }}
                              >
                                <XCircle size={13} /> Final Reject
                              </button>
                              <button
                                onClick={() => setExpandedId(isExpanded ? null : req.id)}
                                className="btn btn-outline"
                                style={{ padding: '0.35rem', fontSize: '0.75rem' }}
                              >
                                {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                              </button>
                            </div>
                          </td>
                        </tr>

                        {isExpanded && (
                          <tr>
                            <td colSpan={6} style={{ backgroundColor: '#f8fafc', padding: '1.25rem' }}>
                              <ApprovalTimeline steps={req.approvalSteps} currentStatus={req.status} escalated={req.escalated} />
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Tab 2: All Org Requests with Filter */}
      {activeTab === 'all' && (
        <div className="glass-panel">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Filter size={18} color="var(--accent-primary)" /> Organization Requests Master Table
            </h3>
            
            {/* Status Filter Dropdown */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Filter Status:</span>
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                style={{ width: 'auto', padding: '0.4rem 0.75rem', fontSize: '0.85rem' }}
              >
                <option value="">All Statuses</option>
                <option value="PENDING_MANAGER">PENDING_MANAGER</option>
                <option value="PENDING_HR">PENDING_HR</option>
                <option value="APPROVED">APPROVED</option>
                <option value="REJECTED">REJECTED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>
          </div>

          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Request ID</th>
                  <th>Employee</th>
                  <th>Type & Reason</th>
                  <th>Dates & Days</th>
                  <th>Status</th>
                  <th>Conflict Risk</th>
                  <th>Timeline</th>
                </tr>
              </thead>
              <tbody>
                {allRequests.map((req) => (
                  <tr key={req.id}>
                    <td style={{ fontWeight: 700, color: 'var(--text-muted)' }}>#{req.id}</td>
                    <td>
                      <div style={{ fontWeight: 700 }}>{req.employeeName}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{req.employeeEmail}</div>
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', flexWrap: 'wrap' }}>
                        <span style={{ fontWeight: 700, color: 'var(--accent-primary)' }}>{req.leaveType}</span>
                        {req.compensatoryWorkingDate && (
                          <span style={{ fontSize: '0.72rem', color: '#854d0e', backgroundColor: '#fef9c3', border: '1px solid #fef08a', padding: '0.1rem 0.35rem', borderRadius: '4px' }}>
                            🗓️ Weekend: {req.compensatoryWorkingDate}
                          </span>
                        )}
                        {req.odType && (
                          <span style={{ fontSize: '0.72rem', color: '#1e40af', backgroundColor: '#dbeafe', border: '1px solid #bfdbfe', padding: '0.1rem 0.35rem', borderRadius: '4px' }}>
                            📍 {req.odType} at {req.location}
                          </span>
                        )}
                      </div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>"{req.reason}"</div>
                      {req.remarks && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Remarks: {req.remarks}</div>
                      )}
                      {req.documentData && (
                        <div style={{ marginTop: '0.35rem' }}>
                          <button
                            type="button"
                            onClick={() => window.open(req.documentData, '_blank')}
                            className="btn btn-outline"
                            style={{ padding: '0.2rem 0.5rem', fontSize: '0.72rem', display: 'inline-flex', alignItems: 'center', gap: '0.3rem', color: '#166534', backgroundColor: '#dcfce7', borderColor: '#86efac' }}
                          >
                            📄 View Document
                          </button>
                        </div>
                      )}
                    </td>
                    <td>
                      {req.leaveType === 'ON_DUTY' ? (
                          <>
                            <div style={{ fontWeight: 600 }}>{req.startDate}</div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{req.startTime} - {req.endTime}</div>
                          </>
                      ) : (
                          <>
                            <div style={{ fontWeight: 600 }}>{req.startDate} to {req.endDate}</div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{req.workingDays} working days</div>
                          </>
                      )}
                    </td>
                    <td>
                      <StatusBadge status={req.status} escalated={req.escalated} />
                    </td>
                    <td>
                      <ConflictBadge flag={req.conflictFlag} severity={req.conflictSeverity} details={req.conflictDetails} />
                    </td>
                    <td>
                      <button
                        onClick={() => setExpandedId(expandedId === req.id ? null : req.id)}
                        className="btn btn-outline"
                        style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                      >
                        Timeline
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 3: Holidays Management */}
      {activeTab === 'holidays' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '2rem' }}>
          
          {/* Create Holiday Form */}
          <div className="glass-panel">
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Plus size={18} color="var(--accent-primary)" /> Add Company Holiday
            </h3>

            <form onSubmit={handleAddHoliday} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>
                  Holiday Date
                </label>
                <input
                  type="date"
                  value={newHolidayDate}
                  onChange={(e) => setNewHolidayDate(e.target.value)}
                  required
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>
                  Holiday Name / Occasion
                </label>
                <input
                  type="text"
                  value={newHolidayName}
                  onChange={(e) => setNewHolidayName(e.target.value)}
                  placeholder="e.g. Independence Day, New Year..."
                  required
                />
              </div>

              <button type="submit" className="btn btn-primary" disabled={addingHoliday} style={{ marginTop: '0.5rem' }}>
                {addingHoliday ? 'Adding...' : 'Save Holiday'}
              </button>
            </form>
          </div>

          {/* Holidays List */}
          <div className="glass-panel">
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Calendar size={18} color="var(--accent-primary)" /> Configured Holidays List
            </h3>

            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Holiday Name</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {holidays.map((h) => (
                    <tr key={h.id}>
                      <td style={{ fontWeight: 700, color: 'var(--accent-primary)' }}>{h.date}</td>
                      <td style={{ fontWeight: 600 }}>{h.name}</td>
                      <td>
                        <button
                          onClick={() => handleDeleteHoliday(h.id)}
                          className="btn btn-danger"
                          style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                        >
                          <Trash2 size={13} /> Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

        </div>
      )}

      {/* Tab 4: Employee Balances Audit */}
      {activeTab === 'balances' && (
        <div className="glass-panel">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Users size={18} color="var(--accent-primary)" /> Employee Leave Balances Audit (2026)
          </h3>

          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Employee ID</th>
                  <th>Employee Name</th>
                  <th>Entitlement</th>
                  <th>Reserved (Pending)</th>
                  <th>Used (Debited)</th>
                  <th>Available</th>
                </tr>
              </thead>
              <tbody>
                {balances.map((b) => (
                  <tr key={b.employeeId}>
                    <td style={{ fontWeight: 700, color: 'var(--text-muted)' }}>#{b.employeeId}</td>
                    <td style={{ fontWeight: 700 }}>{b.employeeName}</td>
                    <td style={{ fontWeight: 700 }}>{b.entitlement} days</td>
                    <td style={{ color: 'var(--warning-color)', fontWeight: 600 }}>{b.reserved} days</td>
                    <td style={{ color: 'var(--info-color)', fontWeight: 600 }}>{b.used} days</td>
                    <td style={{ color: 'var(--success-color)', fontWeight: 800 }}>{b.available} days</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 5: Employee Management */}
      {activeTab === 'employees' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '2rem' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div className="glass-panel">
              <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Plus size={18} color="var(--accent-primary)" /> Manually Add Employee
              </h3>

              <form onSubmit={handleAddEmployee} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Full Name</label>
                  <input type="text" value={newEmp.name} onChange={(e) => setNewEmp({...newEmp, name: e.target.value})} required />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Email</label>
                  <input type="email" value={newEmp.email} onChange={(e) => setNewEmp({...newEmp, email: e.target.value})} required />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Password</label>
                  <input type="text" value={newEmp.plainPassword} onChange={(e) => setNewEmp({...newEmp, plainPassword: e.target.value})} required />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Role</label>
                  <select value={newEmp.role} onChange={(e) => setNewEmp({...newEmp, role: e.target.value})}>
                    <option value="EMPLOYEE">EMPLOYEE</option>
                    <option value="MANAGER">MANAGER</option>
                    <option value="HR">HR</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Team ID</label>
                  <input type="number" value={newEmp.teamId} onChange={(e) => setNewEmp({...newEmp, teamId: Number(e.target.value)})} required />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>Manager ID (Optional)</label>
                  <input type="number" value={newEmp.managerId} onChange={(e) => setNewEmp({...newEmp, managerId: e.target.value})} />
                </div>
                <button type="submit" className="btn btn-primary" disabled={addingEmp}>
                  {addingEmp ? 'Adding...' : 'Add Employee'}
                </button>
              </form>
            </div>

            <div className="glass-panel">
              <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <RefreshCw size={18} color="var(--accent-primary)" /> Upload Excel
              </h3>
              <form onSubmit={handleUploadEmployees} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <input type="file" accept=".xlsx, .xls, .csv" onChange={(e) => setUploadFile(e.target.files[0])} required />
                <button type="submit" className="btn btn-primary" disabled={uploading || !uploadFile}>
                  {uploading ? 'Uploading...' : 'Bulk Add Employees'}
                </button>
              </form>
            </div>
          </div>

          <div className="glass-panel">
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Users size={18} color="var(--accent-primary)" /> Employee Directory
            </h3>
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Password</th>
                    <th>Role</th>
                    <th>Team</th>
                  </tr>
                </thead>
                <tbody>
                  {employees.map((emp) => (
                    <tr key={emp.id}>
                      <td>#{emp.id}</td>
                      <td style={{ fontWeight: 700 }}>{emp.name}</td>
                      <td>{emp.email}</td>
                      <td><code>{emp.plainPassword || '[Encrypted]'}</code></td>
                      <td>
                        <span className="badge" style={{ backgroundColor: '#f1f5f9', color: '#0f172a', border: '1px solid #cbd5e1' }}>
                          {emp.role}
                        </span>
                      </td>
                      <td>Team {emp.teamId}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Approve/Reject Modal Dialog */}
      {actionModal.open && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 800, marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              {actionModal.type === 'approve' ? <CheckCircle2 color="var(--success-color)" /> : <XCircle color="var(--danger-color)" />}
              {actionModal.type === 'approve' ? 'HR Final Approval' : 'HR Final Rejection'}
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
              Perform final action for <strong>{actionModal.employeeName}</strong>.
            </p>

            <div style={{ marginBottom: '1.25rem' }}>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.4rem', color: 'var(--text-secondary)' }}>
                HR Note / Decision Reason
              </label>
              <textarea
                rows={3}
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="Add notes for audit log..."
              />
            </div>

            <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
              <button
                onClick={() => setActionModal({ open: false, type: '', reqId: null, employeeName: '' })}
                className="btn btn-outline"
                disabled={submittingAction}
              >
                Cancel
              </button>
              <button
                onClick={handleAction}
                className={`btn ${actionModal.type === 'approve' ? 'btn-success' : 'btn-danger'}`}
                disabled={submittingAction}
              >
                {submittingAction ? 'Processing...' : actionModal.type === 'approve' ? 'Confirm Final Approval' : 'Confirm Final Rejection'}
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default HrCenter;
