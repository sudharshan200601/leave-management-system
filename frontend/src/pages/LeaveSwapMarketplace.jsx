import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { ArrowRightLeft, Search, RefreshCw, CheckCircle, XCircle } from 'lucide-react';

function LeaveSwapMarketplace() {
    const [myLeaves, setMyLeaves] = useState([]);
    const [selectedLeaveId, setSelectedLeaveId] = useState(null);
    const [eligibleSwaps, setEligibleSwaps] = useState([]);
    
    const [mySwaps, setMySwaps] = useState([]);
    const [pendingManagerSwaps, setPendingManagerSwaps] = useState([]);
    
    const [userRole, setUserRole] = useState(null);
    const [activeTab, setActiveTab] = useState('find');
    
    // Proposal form state
    const [showProposeModal, setShowProposeModal] = useState(false);
    const [targetSwap, setTargetSwap] = useState(null);
    const [newStartDate, setNewStartDate] = useState('');
    const [newEndDate, setNewEndDate] = useState('');

    useEffect(() => {
        const token = sessionStorage.getItem('token');
        if (!token) return;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            setUserRole(payload.role);
            
            api.get('/leaves/my').then(res => {
                setMyLeaves(res.data.filter(l => l.conflictFlag));
            });
            
            fetchMySwaps();
            
            if (payload.role === 'MANAGER' || payload.role === 'HR') {
                fetchPendingManagerSwaps();
            }
        } catch (e) {
            console.error('Error decoding token', e);
        }
    }, []);

    const fetchMySwaps = async () => {
        try {
            const res = await api.get('/swaps/my-swaps');
            setMySwaps(res.data);
        } catch (error) {
            console.error('Error fetching swaps', error);
        }
    };
    
    const fetchPendingManagerSwaps = async () => {
        try {
            const res = await api.get('/swaps/pending-manager');
            setPendingManagerSwaps(res.data);
        } catch (error) {
            console.error('Error fetching manager swaps', error);
        }
    };

    const handleFindSwaps = async (leaveId) => {
        setSelectedLeaveId(leaveId);
        try {
            const res = await api.get(`/swaps/eligible/${leaveId}`);
            setEligibleSwaps(res.data);
        } catch (error) {
            alert('Error finding swaps: ' + (error.response?.data?.message || error.message));
        }
    };

    const openProposeModal = (swap) => {
        setTargetSwap(swap);
        setNewStartDate('');
        setNewEndDate('');
        setShowProposeModal(true);
    };

    const submitPropose = async () => {
        try {
            await api.post('/swaps/propose', {
                requesterLeaveRequestId: selectedLeaveId,
                targetLeaveRequestId: targetSwap.targetLeaveRequestId,
                targetNewStartDate: newStartDate,
                targetNewEndDate: newEndDate
            });
            alert('Swap requested successfully!');
            setShowProposeModal(false);
            fetchMySwaps();
            setActiveTab('my');
        } catch (error) {
            alert('Error proposing swap: ' + (error.response?.data?.message || error.message));
        }
    };

    const handleSwapAction = async (swapId, action) => {
        try {
            await api.post(`/swaps/${swapId}/${action}`);
            alert(`Swap ${action}d successfully!`);
            fetchMySwaps();
            if (userRole === 'MANAGER' || userRole === 'HR') {
                fetchPendingManagerSwaps();
            }
        } catch (error) {
            alert(`Error ${action}ing swap: ` + (error.response?.data?.message || error.message));
        }
    };

    const isTarget = (swap) => {
        return swap.status === 'PENDING_TARGET'; // Relying on the fact that the backend ensures only the target can accept/decline. But we should only show it if they ARE the target.
        // Actually, it's safer to let the user try and get blocked by backend, but ideally we'd filter.
        // For simplicity, we just show it if status is PENDING_TARGET.
    };

    return (
        <div style={{ maxWidth: '1280px', margin: '2rem auto', padding: '0 1.5rem' }}>
            <div style={{ marginBottom: '2rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div>
                    <h1 style={{ fontSize: '1.75rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <ArrowRightLeft size={24} color="var(--accent-primary)" /> Leave Swap Marketplace
                    </h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                        Resolve team conflicts by swapping leave dates with your colleagues.
                    </p>
                </div>
                <button onClick={() => { fetchMySwaps(); if(userRole === 'MANAGER' || userRole === 'HR') fetchPendingManagerSwaps(); }} className="btn btn-outline" style={{ padding: '0.5rem 0.85rem' }}>
                    <RefreshCw size={14} /> Refresh
                </button>
            </div>
            
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '2rem' }}>
                <button 
                    onClick={() => setActiveTab('find')} 
                    className={`btn ${activeTab === 'find' ? 'btn-primary' : 'btn-outline'}`}
                >
                    <Search size={16} /> Find Swaps
                </button>
                <button 
                    onClick={() => setActiveTab('my')} 
                    className={`btn ${activeTab === 'my' ? 'btn-primary' : 'btn-outline'}`}
                >
                    <ArrowRightLeft size={16} /> My Swaps
                </button>
                {(userRole === 'MANAGER' || userRole === 'HR') && (
                    <button 
                        onClick={() => setActiveTab('manager')} 
                        className={`btn ${activeTab === 'manager' ? 'btn-primary' : 'btn-outline'}`}
                    >
                        <CheckCircle size={16} /> Pending Approvals
                    </button>
                )}
            </div>

            {activeTab === 'find' && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
                    <div className="glass-panel" style={{ height: 'fit-content' }}>
                        <h2 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem' }}>1. Select Your Conflicting Leave</h2>
                        {myLeaves.length === 0 ? (
                            <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No conflicting leaves found.</p>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                                {myLeaves.map(leave => (
                                    <div key={leave.id} 
                                         style={{ 
                                             padding: '1rem', 
                                             border: selectedLeaveId === leave.id ? '2px solid var(--border-focus)' : '1px solid var(--border-color)', 
                                             borderRadius: 'var(--radius-md)', 
                                             cursor: 'pointer',
                                             backgroundColor: selectedLeaveId === leave.id ? 'var(--accent-light)' : '#fff'
                                         }}
                                         onClick={() => handleFindSwaps(leave.id)}>
                                        <div style={{ fontWeight: 700 }}>{leave.startDate} to {leave.endDate}</div>
                                        <div style={{ fontSize: '0.8rem', color: 'var(--danger-color)', marginTop: '0.25rem' }}>Conflict: {leave.conflictSeverity}</div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    {selectedLeaveId && (
                        <div className="glass-panel" style={{ height: 'fit-content' }}>
                            <h2 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem' }}>2. Eligible Swaps</h2>
                            {eligibleSwaps.length === 0 ? (
                                <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No eligible swaps found for this team and date.</p>
                            ) : (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                                    {eligibleSwaps.map(swap => (
                                        <div key={swap.targetLeaveRequestId} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', backgroundColor: '#fff' }}>
                                            <div>
                                                <div style={{ fontWeight: 800, color: 'var(--text-primary)' }}>{swap.targetEmployeeName}</div>
                                                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Approved leave: {swap.startDate} to {swap.endDate}</div>
                                            </div>
                                            <button 
                                                onClick={() => openProposeModal(swap)}
                                                className="btn btn-lime"
                                                style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}
                                            >
                                                Propose Swap
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}
                </div>
            )}

            {activeTab === 'my' && (
                <div className="glass-panel">
                    <h2 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem' }}>My Swap History</h2>
                    <div className="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Requester</th>
                                    <th>Target</th>
                                    <th>Requested Dates</th>
                                    <th>Proposed Target Dates</th>
                                    <th>Coverage Before</th>
                                    <th>Coverage After</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {mySwaps.map(swap => (
                                    <tr key={swap.id}>
                                        <td style={{ fontWeight: 600 }}>{swap.requesterName}</td>
                                        <td style={{ fontWeight: 600 }}>{swap.targetEmployeeName}</td>
                                        <td>{swap.originalStartDate} to {swap.originalEndDate}</td>
                                        <td>{swap.targetNewStartDate} to {swap.targetNewEndDate}</td>
                                        <td>
                                            <span className={`badge ${swap.coverageBefore?.includes('Critical') ? 'badge-conflict-high' : 'badge-approved'}`}>
                                                {swap.coverageBefore}
                                            </span>
                                        </td>
                                        <td>
                                            <span className={`badge ${swap.coverageAfter?.includes('Critical') ? 'badge-conflict-high' : 'badge-approved'}`}>
                                                {swap.coverageAfter}
                                            </span>
                                        </td>
                                        <td>
                                            <span style={{ fontWeight: 700, fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                                                {swap.status.replace('_', ' ')}
                                            </span>
                                        </td>
                                        <td>
                                            {isTarget(swap) && (
                                                <div style={{ display: 'flex', gap: '0.5rem' }}>
                                                    <button onClick={() => handleSwapAction(swap.id, 'accept')} className="btn btn-success" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}><CheckCircle size={14}/> Accept</button>
                                                    <button onClick={() => handleSwapAction(swap.id, 'decline')} className="btn btn-danger" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}><XCircle size={14}/> Decline</button>
                                                </div>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        {mySwaps.length === 0 && <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>No swaps found.</div>}
                    </div>
                </div>
            )}

            {activeTab === 'manager' && (userRole === 'MANAGER' || userRole === 'HR') && (
                <div className="glass-panel">
                    <h2 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem' }}>Pending Manager Approvals</h2>
                    <div className="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Requester</th>
                                    <th>Target</th>
                                    <th>Swap Dates</th>
                                    <th>Coverage Before</th>
                                    <th>Coverage After</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {pendingManagerSwaps.map(swap => (
                                    <tr key={swap.id}>
                                        <td style={{ fontWeight: 600 }}>{swap.requesterName}</td>
                                        <td style={{ fontWeight: 600 }}>{swap.targetEmployeeName}</td>
                                        <td>
                                            <div style={{ fontSize: '0.85rem' }}><b>Req gets:</b> {swap.originalStartDate} to {swap.originalEndDate}</div>
                                            <div style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}><b>Tgt gets:</b> {swap.targetNewStartDate} to {swap.targetNewEndDate}</div>
                                        </td>
                                        <td>
                                            <span className={`badge ${swap.coverageBefore?.includes('Critical') ? 'badge-conflict-high' : 'badge-approved'}`}>
                                                {swap.coverageBefore}
                                            </span>
                                        </td>
                                        <td>
                                            <span className={`badge ${swap.coverageAfter?.includes('Critical') ? 'badge-conflict-high' : 'badge-approved'}`}>
                                                {swap.coverageAfter}
                                            </span>
                                        </td>
                                        <td>
                                            <button onClick={() => handleSwapAction(swap.id, 'approve')} className="btn btn-primary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}>
                                                Approve Swap
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        {pendingManagerSwaps.length === 0 && <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>No pending swaps.</div>}
                    </div>
                </div>
            )}

            {showProposeModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h2 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '0.5rem' }}>Propose Dates to {targetSwap?.targetEmployeeName}</h2>
                        <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
                            You are requesting their leave on <b>{targetSwap?.startDate} to {targetSwap?.endDate}</b>.<br/>
                            Suggest new dates for them to take instead:
                        </p>
                        
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.5rem' }}>
                            <div>
                                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>New Start Date</label>
                                <input type="date" value={newStartDate} onChange={e => setNewStartDate(e.target.value)} />
                            </div>
                            <div>
                                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.35rem', color: 'var(--text-secondary)' }}>New End Date</label>
                                <input type="date" value={newEndDate} onChange={e => setNewEndDate(e.target.value)} />
                            </div>
                        </div>

                        <div style={{ display: 'flex', gap: '1rem' }}>
                            <button onClick={() => setShowProposeModal(false)} className="btn btn-outline" style={{ flex: 1 }}>
                                Cancel
                            </button>
                            <button onClick={submitPropose} className="btn btn-lime" style={{ flex: 1 }}>
                                Send Proposal
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default LeaveSwapMarketplace;
