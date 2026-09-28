import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/StatusBadge';
import ConflictBadge from '../components/ConflictBadge';
import ApprovalTimeline from '../components/ApprovalTimeline';
import { Calendar, PlusCircle, CheckCircle2, Clock, Ban, AlertCircle, RefreshCw, ChevronDown, ChevronUp } from 'lucide-react';

const EmployeeWorkspace = () => {
  const { user } = useAuth();
  const [balance, setBalance] = useState(null);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);
  const navigate = useNavigate();

  // Form state
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [leaveType, setLeaveType] = useState('ANNUAL');
  const [reason, setReason] = useState('');
  const [compensatoryWorkingDate, setCompensatoryWorkingDate] = useState('');
  const [documentName, setDocumentName] = useState('');
  const [documentData, setDocumentData] = useState('');
  const [odType, setOdType] = useState('');
  const [location, setLocation] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [remarks, setRemarks] = useState('');
  const [fileError, setFileError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');
  const [formSuccess, setFormSuccess] = useState('');
  const [calculatedWorkingDays, setCalculatedWorkingDays] = useState(0);

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    // Live Working Days Calculation
    if (startDate && endDate && startDate <= endDate) {
      let count = 0;
      let cur = new Date(startDate);
      const end = new Date(endDate);
      while (cur <= end) {
        const day = cur.getDay(); // 0 = Sun, 6 = Sat
        if (day !== 0 && day !== 6) {
          count++;
        }
        cur.setDate(cur.getDate() + 1);
      }
      setCalculatedWorkingDays(count);
    } else {
      setCalculatedWorkingDays(0);
    }
  }, [startDate, endDate]);

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setFileError('');

    const isOd = leaveType === 'ON_DUTY';
    const isMedical = leaveType === 'MEDICAL';
    
    if (isMedical && file.type !== 'application/pdf' && !file.name.toLowerCase().endsWith('.pdf')) {
      setFileError('Invalid file type. Only PDF documents are allowed for Medical Leave.');
      e.target.value = '';
      return;
    }

    if (isOd) {
      const validTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
      if (!validTypes.includes(file.type)) {
        setFileError('Invalid file type. Only PDF, JPG, JPEG, and PNG are allowed for OD.');
        e.target.value = '';
        return;
      }
    }

    if (file.size > 100 * 1024) {
      setFileError(`File size (${(file.size / 1024).toFixed(1)} KB) exceeds 100 KB limit.`);
      e.target.value = '';
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      setDocumentData(reader.result);
      setDocumentName(file.name);
    };
    reader.readAsDataURL(file);
  };

  const removeAttachedDocument = () => {
    setDocumentData('');
    setDocumentName('');
    setFileError('');
  };

  const fetchData = async () => {
    setLoading(true);
    try {
      const [balRes, reqRes] = await Promise.all([
        api.get('/leaves/balance'),
        api.get('/leaves/my')
      ]);
      setBalance(balRes.data);
      setRequests(reqRes.data);
    } catch (err) {
      console.error('Failed to fetch employee data', err);
    } finally {
      setLoading(false);
    }
  };

  const handleApplyLeave = async (e) => {
    e.preventDefault();
    setFormError('');
    setFormSuccess('');

    if (calculatedWorkingDays <= 0) {
      setFormError('Selected date range contains 0 working days (weekends or holidays).');
      return;
    }

    if (leaveType === 'ANNUAL' && balance && calculatedWorkingDays > balance.available) {
      setFormError(`Insufficient annual leave balance. Required: ${calculatedWorkingDays}, Available: ${balance.available}`);
      return;
    }

    if (leaveType === 'COMP_OFF') {
      if (!compensatoryWorkingDate) {
        setFormError('Please select a compensatory working weekend date for Comp-off.');
        return;
      }
      const compDate = new Date(compensatoryWorkingDate + 'T00:00:00');
      const day = compDate.getDay();
      if (day !== 0 && day !== 6) {
        setFormError('Compensatory working date must be a weekend (Saturday or Sunday).');
        return;
      }
    }

    if (leaveType === 'MEDICAL' && !documentData) {
      setFormError('Please attach a medical certificate (PDF under 100 KB) for medical leave.');
      return;
    }

    if (leaveType === 'ON_DUTY') {
      if (!odType || !location || !startTime || !endTime || !reason) {
        setFormError('Please fill in all required OD fields (Type, Location, Start/End Time, Reason).');
        return;
      }
      if (startDate < new Date().toISOString().split('T')[0]) {
        setFormError('OD cannot be applied for a past date. Please select today or a future date.');
        return;
      }
    }

    setSubmitting(true);
    try {
      await api.post('/leaves', {
        startDate,
        endDate: leaveType === 'ON_DUTY' ? startDate : endDate,
        leaveType,
        reason,
        compensatoryWorkingDate: leaveType === 'COMP_OFF' ? compensatoryWorkingDate : null,
        documentName: documentData ? documentName : null,
        documentData: documentData ? documentData : null,
        odType: leaveType === 'ON_DUTY' ? odType : null,
        location: leaveType === 'ON_DUTY' ? location : null,
        remarks: leaveType === 'ON_DUTY' ? remarks : null,
        startTime: leaveType === 'ON_DUTY' ? startTime : null,
        endTime: leaveType === 'ON_DUTY' ? endTime : null,
      });
      setFormSuccess('Request submitted successfully!');
      setStartDate('');
      setEndDate('');
      setReason('');
      setCompensatoryWorkingDate('');
      setDocumentName('');
      setDocumentData('');
      setOdType('');
      setLocation('');
      setRemarks('');
      setStartTime('');
      setEndTime('');
      fetchData();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to submit leave request.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancelRequest = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this leave request?')) return;
    try {
      await api.post(`/leaves/${id}/cancel`);
      fetchData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel leave request');
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
        <RefreshCw className="spin" size={32} />
        <p style={{ marginTop: '1rem' }}>Loading leave workspace...</p>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1280px', margin: '2rem auto', padding: '0 1.5rem' }}>
      
      {/* Page Header */}
      <div style={{ marginBottom: '2rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800 }}>My Leave Workspace</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Submit leave requests, check your pro-rated quota, and track real-time approval status.
          </p>
        </div>
        <button onClick={fetchData} className="btn btn-outline" style={{ padding: '0.5rem 0.85rem' }}>
          <RefreshCw size={14} /> Refresh
        </button>
      </div>

      {/* Balance Metric Cards */}
      {balance && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.25rem', marginBottom: '2rem' }}>
          
          <div className="glass-panel" style={{ borderLeft: '4px solid #0f172a' }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Annual Entitlement
            </div>
            <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.3rem 0', color: '#0f172a' }}>
              {balance.entitlement} <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)', fontWeight: 500 }}>days</span>
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
              Pro-rated for join date ({user.email})
            </div>
          </div>

          <div className="glass-panel" style={{ borderLeft: '4px solid #84cc16' }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#3f6212', textTransform: 'uppercase' }}>
              Available Balance
            </div>
            <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.3rem 0', color: '#3f6212' }}>
              {balance.available} <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)', fontWeight: 500 }}>days</span>
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
              Ready to use for annual leave
            </div>
          </div>

          <div className="glass-panel" style={{ borderLeft: '4px solid #eab308' }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#854d0e', textTransform: 'uppercase' }}>
              Reserved (Pending)
            </div>
            <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.3rem 0', color: '#854d0e' }}>
              {balance.reserved} <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)', fontWeight: 500 }}>days</span>
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
              Locked in active approval chains
            </div>
          </div>

          <div className="glass-panel" style={{ borderLeft: '4px solid #64748b' }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Used (Debited)
            </div>
            <div style={{ fontSize: '2rem', fontWeight: 800, margin: '0.3rem 0', color: '#334155' }}>
              {balance.used} <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)', fontWeight: 500 }}>days</span>
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
              Final approved leave days
            </div>
          </div>

        </div>
      )}

      {/* Main Grid: Form + History */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '2rem' }}>
        
        {/* Left: Apply Leave Form */}
        <div className="glass-panel" style={{ height: 'fit-content' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <PlusCircle size={18} color="var(--accent-primary)" /> Apply for Leave
          </h3>

          {formError && (
            <div style={{ backgroundColor: 'var(--danger-bg)', color: 'var(--danger-color)', padding: '0.75rem', borderRadius: 'var(--radius-md)', fontSize: '0.85rem', marginBottom: '1rem' }}>
              {formError}
            </div>
          )}

          {formSuccess && (
            <div style={{ backgroundColor: 'var(--success-bg)', color: 'var(--success-color)', padding: '0.75rem', borderRadius: 'var(--radius-md)', fontSize: '0.85rem', marginBottom: '1rem' }}>
              {formSuccess}
            </div>
          )}

          <form onSubmit={handleApplyLeave} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>
                Leave Type
              </label>
              <select value={leaveType} onChange={(e) => setLeaveType(e.target.value)}>
                <option value="ANNUAL">Annual Leave (From Quota)</option>
                <option value="SICK">Sick Leave</option>
                <option value="COMP_OFF">Compensatory Off (Comp-off)</option>
                <option value="MEDICAL">Medical Leave (Requires PDF up to 100KB)</option>
                <option value="ON_DUTY">On Duty (OD)</option>
                <option value="UNPAID">Unpaid Leave</option>
              </select>
            </div>

            {/* Compensatory Working Date Picker for COMP_OFF */}
            {leaveType === 'COMP_OFF' && (
              <div style={{ backgroundColor: '#fefce8', border: '1px solid #fef08a', padding: '0.85rem', borderRadius: 'var(--radius-md)' }}>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: '#854d0e' }}>
                  🗓️ Compensatory Working Weekend Date *
                </label>
                <input
                  type="date"
                  value={compensatoryWorkingDate}
                  onChange={(e) => setCompensatoryWorkingDate(e.target.value)}
                  required
                />
                <p style={{ fontSize: '0.75rem', color: '#a16207', marginTop: '0.35rem' }}>
                  Select the Saturday or Sunday you will work in exchange for taking this leave.
                </p>
              </div>
            )}

            {/* Medical PDF Certificate Upload for MEDICAL */}
            {leaveType === 'MEDICAL' && (
              <div style={{ backgroundColor: '#f0fdf4', border: '1px solid #bbf7d0', padding: '0.85rem', borderRadius: 'var(--radius-md)' }}>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: '#166534' }}>
                  📄 Upload Medical Certificate (PDF only, max 100 KB) *
                </label>
                <input
                  type="file"
                  accept=".pdf,application/pdf"
                  onChange={handleFileUpload}
                  style={{ fontSize: '0.8rem' }}
                />
                {fileError && (
                  <p style={{ fontSize: '0.75rem', color: 'var(--danger-color)', marginTop: '0.35rem', fontWeight: 600 }}>
                    {fileError}
                  </p>
                )}
                {documentName && !fileError && (
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '0.5rem', padding: '0.35rem 0.6rem', backgroundColor: '#dcfce7', borderRadius: '4px', fontSize: '0.75rem', color: '#14532d' }}>
                    <span>📎 <strong>{documentName}</strong> attached</span>
                    <button type="button" onClick={removeAttachedDocument} style={{ background: 'none', border: 'none', color: '#dc2626', cursor: 'pointer', fontWeight: 700 }}>✕ Remove</button>
                  </div>
                )}
                <p style={{ fontSize: '0.72rem', color: '#15803d', marginTop: '0.35rem' }}>
                  Document will be securely viewable by your manager and HR.
                </p>
              </div>
            )}

            {leaveType === 'ON_DUTY' && (
              <div style={{ backgroundColor: '#eff6ff', border: '1px solid #bfdbfe', padding: '0.85rem', borderRadius: 'var(--radius-md)', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: '#1e3a8a' }}>OD Date *</label>
                    <input type="date" value={startDate} onChange={(e) => { setStartDate(e.target.value); setEndDate(e.target.value); }} min={new Date().toISOString().split('T')[0]} required />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: '#1e3a8a' }}>OD Type *</label>
                    <select value={odType} onChange={(e) => setOdType(e.target.value)} required>
                      <option value="">Select...</option>
                      <option value="Site Visit">Site Visit</option>
                      <option value="Client Visit">Client Visit</option>
                      <option value="Official Meeting">Official Meeting</option>
                      <option value="Training">Training</option>
                      <option value="Workshop/Seminar">Workshop/Seminar</option>
                      <option value="Company/College Event">Company/College Event</option>
                      <option value="Other">Other</option>
                    </select>
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: '#1e3a8a' }}>Start Time *</label>
                    <input type="time" value={startTime} onChange={(e) => setStartTime(e.target.value)} required />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: '#1e3a8a' }}>End Time *</label>
                    <input type="time" value={endTime} onChange={(e) => setEndTime(e.target.value)} required />
                  </div>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: '#1e3a8a' }}>Location *</label>
                  <input type="text" value={location} onChange={(e) => setLocation(e.target.value)} required />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: '#1e3a8a' }}>Purpose / Reason *</label>
                  <textarea rows={2} value={reason} onChange={(e) => setReason(e.target.value)} required />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: '#1e3a8a' }}>Remarks (Optional)</label>
                  <textarea rows={2} value={remarks} onChange={(e) => setRemarks(e.target.value)} />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: '#1e3a8a' }}>
                    📎 Supporting Document (Optional) - PDF, JPG, PNG (Max 100KB)
                  </label>
                  <input type="file" accept=".pdf,image/jpeg,image/jpg,image/png" onChange={handleFileUpload} />
                  {fileError && <p style={{ fontSize: '0.75rem', color: 'var(--danger-color)', marginTop: '0.35rem', fontWeight: 600 }}>{fileError}</p>}
                  {documentName && !fileError && (
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '0.5rem', padding: '0.35rem 0.6rem', backgroundColor: '#dbeafe', borderRadius: '4px', fontSize: '0.75rem', color: '#1e40af' }}>
                      <span>📎 <strong>{documentName}</strong> attached</span>
                      <button type="button" onClick={removeAttachedDocument} style={{ background: 'none', border: 'none', color: '#dc2626', cursor: 'pointer', fontWeight: 700 }}>✕ Remove</button>
                    </div>
                  )}
                </div>
              </div>
            )}

            {leaveType !== 'ON_DUTY' && (
              <>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>
                      Start Date
                    </label>
                    <input
                      type="date"
                      value={startDate}
                      onChange={(e) => setStartDate(e.target.value)}
                      required
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>
                      End Date
                    </label>
                    <input
                      type="date"
                      value={endDate}
                      onChange={(e) => setEndDate(e.target.value)}
                      required
                    />
                  </div>
                </div>

                {/* Live Working Days Calculator Widget */}
                <div style={{
                  backgroundColor: '#f8fafc',
                  border: '1px solid var(--border-color)',
                  padding: '0.75rem 1rem',
                  borderRadius: 'var(--radius-md)',
                  display: 'flex',
                  alignItems: 'center',
                  justify: 'space-between'
                }}>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Calculated Working Days:</span>
                  <span style={{ fontSize: '1.1rem', fontWeight: 800, color: calculatedWorkingDays > 0 ? '#4d7c0f' : 'var(--text-muted)' }}>
                    {calculatedWorkingDays} {calculatedWorkingDays === 1 ? 'day' : 'days'}
                  </span>
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>
                    Reason / Justification
                  </label>
                  <textarea
                    rows={3}
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    placeholder="State your reason for leave..."
                    required
                  />
                </div>
              </>
            )}

            <button type="submit" className="btn btn-primary" disabled={submitting} style={{ marginTop: '0.5rem' }}>
              {submitting ? 'Submitting...' : 'Submit Request'}
            </button>
          </form>
        </div>

        {/* Right: My Requests History */}
        <div className="glass-panel">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Calendar size={18} color="var(--accent-primary)" /> My Request History
          </h3>

          {requests.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--text-muted)' }}>
              <Clock size={40} style={{ opacity: 0.4, marginBottom: '1rem' }} />
              <p>No leave requests submitted yet.</p>
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Type</th>
                    <th>Date Range</th>
                    <th>Days</th>
                    <th>Status</th>
                    <th>Team Conflict</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {requests.map((req) => {
                    const isExpanded = expandedId === req.id;
                    const isCancelable = req.status === 'PENDING_MANAGER' || req.status === 'PENDING_HR' ||
                      (req.status === 'APPROVED' && new Date(req.startDate) > new Date());

                    return (
                      <React.Fragment key={req.id}>
                        <tr>
                          <td>
                            <div style={{ fontWeight: 700 }}>{req.leaveType}</div>
                            {req.compensatoryWorkingDate && (
                              <div style={{ fontSize: '0.72rem', color: '#854d0e', backgroundColor: '#fef9c3', border: '1px solid #fef08a', padding: '0.15rem 0.4rem', borderRadius: '4px', marginTop: '0.25rem', display: 'inline-block' }}>
                                🗓️ Weekend: {req.compensatoryWorkingDate}
                              </div>
                            )}
                            {req.odType && (
                              <div style={{ fontSize: '0.72rem', color: '#1e40af', backgroundColor: '#dbeafe', border: '1px solid #bfdbfe', padding: '0.15rem 0.4rem', borderRadius: '4px', marginTop: '0.25rem', display: 'inline-block' }}>
                                📍 {req.odType} at {req.location}
                              </div>
                            )}
                            {req.documentData && (
                              <div style={{ marginTop: '0.25rem' }}>
                                <button
                                  type="button"
                                  onClick={() => window.open(req.documentData, '_blank')}
                                  className="btn btn-outline"
                                  style={{ padding: '0.2rem 0.45rem', fontSize: '0.7rem', display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}
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
                                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>"{req.reason}"</div>
                              </>
                            ) : (
                              <>
                                <div style={{ fontWeight: 600 }}>{req.startDate} to {req.endDate}</div>
                                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>"{req.reason}"</div>
                              </>
                            )}
                          </td>
                          <td style={{ fontWeight: 700, color: 'var(--accent-primary)' }}>
                            {req.workingDays}
                          </td>
                          <td>
                            <StatusBadge status={req.status} escalated={req.escalated} />
                          </td>
                          <td>
                            <ConflictBadge flag={req.conflictFlag} severity={req.conflictSeverity} details={req.conflictDetails} />
                          </td>
                          <td>
                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                              <button
                                onClick={() => setExpandedId(isExpanded ? null : req.id)}
                                className="btn btn-outline"
                                style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                              >
                                {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />} Timeline
                              </button>
                              {isCancelable && (
                                <button
                                  onClick={() => handleCancelRequest(req.id)}
                                  className="btn btn-danger"
                                  style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
                                >
                                  Cancel
                                </button>
                              )}
                              {req.conflictFlag && (
                                <button
                                  onClick={() => navigate('/swap-marketplace')}
                                  className="btn"
                                  style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', backgroundColor: '#c5f013', color: '#000', fontWeight: 'bold' }}
                                >
                                  Find Swap
                                </button>
                              )}
                            </div>
                          </td>
                        </tr>

                        {/* Expanded Approval Steps Timeline */}
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

      </div>
    </div>
  );
};

export default EmployeeWorkspace;
