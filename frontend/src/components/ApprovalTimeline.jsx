import React from 'react';
import { CheckCircle2, XCircle, Clock, AlertCircle, User, Shield } from 'lucide-react';

const ApprovalTimeline = ({ steps, currentStatus, escalated }) => {
  const getStepStatus = (level) => {
    const step = steps?.find((s) => s.level === level);
    if (step) return step;
    return null;
  };

  const managerStep = getStepStatus('MANAGER');
  const hrStep = getStepStatus('HR');

  return (
    <div style={{ padding: '0.75rem 0' }}>
      <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', marginBottom: '0.75rem', textTransform: 'uppercase' }}>
        Approval Workflow Timeline
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        
        {/* Step 1: Submission */}
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
          <div style={{ 
            width: '28px', height: '28px', borderRadius: '50%', background: 'var(--success-bg)', 
            color: 'var(--success-color)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 
          }}>
            <CheckCircle2 size={16} />
          </div>
          <div>
            <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>1. Submitted</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Leave request created and reserved</div>
          </div>
        </div>

        {/* Step 2: Manager Approval */}
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
          <div style={{ 
            width: '28px', height: '28px', borderRadius: '50%', 
            background: managerStep ? (managerStep.decision === 'APPROVED' ? 'var(--success-bg)' : managerStep.decision === 'REJECTED' ? 'var(--danger-bg)' : 'rgba(236,72,153,0.2)') : (currentStatus === 'PENDING_MANAGER' ? 'var(--warning-bg)' : '#f1f5f9'), 
            color: managerStep ? (managerStep.decision === 'APPROVED' ? 'var(--success-color)' : managerStep.decision === 'REJECTED' ? 'var(--danger-color)' : '#ec4899') : (currentStatus === 'PENDING_MANAGER' ? 'var(--warning-color)' : 'var(--text-muted)'),
            display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 
          }}>
            {managerStep ? (
              managerStep.decision === 'APPROVED' ? <CheckCircle2 size={16} /> :
              managerStep.decision === 'REJECTED' ? <XCircle size={16} /> : <AlertCircle size={16} />
            ) : (
              currentStatus === 'PENDING_MANAGER' ? <Clock size={16} /> : <User size={16} />
            )}
          </div>
          <div>
            <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>
              2. Manager Approval
              {managerStep && <span style={{ marginLeft: '0.5rem', fontSize: '0.75rem', opacity: 0.8 }}>({managerStep.decision})</span>}
            </div>
            {managerStep ? (
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                By {managerStep.approverName} - "{managerStep.comment}"
              </div>
            ) : (
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                {currentStatus === 'PENDING_MANAGER' ? 'Awaiting Manager review...' : 'Bypassed / Escalated'}
              </div>
            )}
          </div>
        </div>

        {/* Step 3: HR Approval */}
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
          <div style={{ 
            width: '28px', height: '28px', borderRadius: '50%', 
            background: hrStep ? (hrStep.decision === 'APPROVED' ? 'var(--success-bg)' : hrStep.decision === 'REJECTED' ? 'var(--danger-bg)' : 'rgba(236,72,153,0.2)') : (currentStatus === 'PENDING_HR' ? 'var(--info-bg)' : '#f1f5f9'), 
            color: hrStep ? (hrStep.decision === 'APPROVED' ? 'var(--success-color)' : hrStep.decision === 'REJECTED' ? 'var(--danger-color)' : '#ec4899') : (currentStatus === 'PENDING_HR' ? 'var(--info-color)' : 'var(--text-muted)'),
            display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 
          }}>
            {hrStep ? (
              hrStep.decision === 'APPROVED' ? <CheckCircle2 size={16} /> :
              hrStep.decision === 'REJECTED' ? <XCircle size={16} /> : <AlertCircle size={16} />
            ) : (
              currentStatus === 'PENDING_HR' ? <Clock size={16} /> : <Shield size={16} />
            )}
          </div>
          <div>
            <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>
              3. HR Final Approval
              {hrStep && <span style={{ marginLeft: '0.5rem', fontSize: '0.75rem', opacity: 0.8 }}>({hrStep.decision})</span>}
            </div>
            {hrStep ? (
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                By {hrStep.approverName} - "{hrStep.comment}"
              </div>
            ) : (
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                {currentStatus === 'PENDING_HR' ? (escalated ? 'Escalated - Awaiting HR final review' : 'Awaiting HR final review...') : 'Pending previous steps'}
              </div>
            )}
          </div>
        </div>

      </div>
    </div>
  );
};

export default ApprovalTimeline;
