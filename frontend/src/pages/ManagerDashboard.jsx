import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import StatusBadge from '../components/StatusBadge';
import ConflictBadge from '../components/ConflictBadge';
import ApprovalTimeline from '../components/ApprovalTimeline';
import { UserCheck, CheckCircle2, XCircle, Calendar, RefreshCw, MessageSquare, AlertTriangle, ChevronDown, ChevronUp } from 'lucide-react';

const ManagerDashboard = () => {
  const [pendingRequests, setPendingRequests] = useState([]);
  const [calendarEvents, setCalendarEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);

  // Modal dialog state
  const [actionModal, setActionModal] = useState({ open: false, type: '', reqId: null, employeeName: '' });
  const [comment, setComment] = useState('');
  const [submittingAction, setSubmittingAction] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [pendingRes, calRes] = await Promise.all([
        api.get('/manager/pending'),
        api.get('/manager/team-calendar')
      ]);
      setPendingRequests(pendingRes.data);
      setCalendarEvents(calRes.data);
    } catch (err) {
      console.error('Failed to load manager dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async () => {
    if (!actionModal.reqId || !actionModal.type) return;
    setSubmittingAction(true);
    try {
      const endpoint = `/manager/${actionModal.reqId}/${actionModal.type}`;
      await api.post(endpoint, { comment });
      setActionModal({ open: false, type: '', reqId: null, employeeName: '' });
      setComment('');
      fetchData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to submit manager decision');
    } finally {
      setSubmittingAction(false);
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
        <RefreshCw className="spin" size={32} />
        <p style={{ marginTop: '1rem' }}>Loading Manager Dashboard...</p>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1280px', margin: '2rem auto', padding: '0 1.5rem' }}>
      
      {/* Header */}
      <div style={{ marginBottom: '2rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>Manager Team Dashboard</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Review pending team requests, inspect conflict overload warnings, and monitor team calendar.
          </p>
        </div>
        <button onClick={fetchData} className="btn btn-outline" style={{ padding: '0.5rem 0.85rem' }}>
          <RefreshCw size={14} /> Refresh Queue
        </button>
      </div>

      {/* Grid: Pending Approvals Queue + Team Calendar */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '2rem' }}>
        
        {/* Left: Pending Team Approvals Queue */}
        <div className="glass-panel">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <UserCheck size={18} color="var(--accent-primary)" /> Pending Team Approvals
            </h3>
            <span className="badge badge-pending-manager">
              {pendingRequests.length} Pending
            </span>
          </div>

          {pendingRequests.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--text-muted)' }}>
              <CheckCircle2 size={40} style={{ opacity: 0.4, color: 'var(--success-color)', marginBottom: '1rem' }} />
              <p style={{ fontWeight: 600 }}>All team approvals cleared!</p>
              <p style={{ fontSize: '0.8rem', opacity: 0.7 }}>No pending direct report requests at this time.</p>
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
                            <div style={{ display: 'flex', gap: '0.35rem' }}>
                              <button
                                onClick={() => setActionModal({ open: true, type: 'approve', reqId: req.id, employeeName: req.employeeName })}
                                className="btn btn-success"
                                style={{ padding: '0.35rem 0.65rem', fontSize: '0.75rem' }}
                              >
                                <CheckCircle2 size={13} /> Approve
                              </button>
                              <button
                                onClick={() => setActionModal({ open: true, type: 'reject', reqId: req.id, employeeName: req.employeeName })}
                                className="btn btn-danger"
                                style={{ padding: '0.35rem 0.65rem', fontSize: '0.75rem' }}
                              >
                                <XCircle size={13} /> Reject
                              </button>
                              <button
                                onClick={() => setExpandedId(isExpanded ? null : req.id)}
                                className="btn btn-outline"
                                style={{ padding: '0.35rem', fontSize: '0.75rem' }}
                                title="Toggle Timeline"
                              >
                                {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                              </button>
                            </div>
                          </td>
                        </tr>

                        {isExpanded && (
                          <tr>
                            <td colSpan={5} style={{ backgroundColor: '#f8fafc', padding: '1.25rem' }}>
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

        {/* Right: Team Calendar Widget */}
        <div className="glass-panel">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Calendar size={18} color="var(--accent-primary)" /> Team Leave Schedule
          </h3>

          {calendarEvents.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No team leaves scheduled.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {calendarEvents.map((evt) => (
                <div
                  key={evt.id}
                  style={{
                    backgroundColor: '#f8fafc',
                    border: '1px solid var(--border-color)',
                    padding: '0.75rem 1rem',
                    borderRadius: 'var(--radius-md)',
                    display: 'flex',
                    alignItems: 'center',
                    justify: 'space-between'
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '0.85rem' }}>{evt.employeeName}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      {evt.startDate} → {evt.endDate} ({evt.leaveType})
                    </div>
                  </div>
                  <StatusBadge status={evt.status} />
                </div>
              ))}
            </div>
          )}
        </div>

      </div>

      {/* Approve/Reject Modal Dialog */}
      {actionModal.open && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3 style={{ fontSize: '1.2rem', fontWeight: 800, marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              {actionModal.type === 'approve' ? <CheckCircle2 color="var(--success-color)" /> : <XCircle color="var(--danger-color)" />}
              {actionModal.type === 'approve' ? 'Manager Approval' : 'Manager Rejection'}
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
              {actionModal.type === 'approve' ? 'Approve leave request for' : 'Reject leave request for'} <strong>{actionModal.employeeName}</strong>.
            </p>

            <div style={{ marginBottom: '1.25rem' }}>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.4rem', color: 'var(--text-secondary)' }}>
                Decision Comment / Note
              </label>
              <textarea
                rows={3}
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="Add optional notes for applicant & HR..."
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
                {submittingAction ? 'Processing...' : actionModal.type === 'approve' ? 'Confirm Approval' : 'Confirm Rejection'}
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default ManagerDashboard;
