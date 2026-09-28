import React from 'react';
import { Clock, CheckCircle2, XCircle, Ban, AlertCircle } from 'lucide-react';

const StatusBadge = ({ status, escalated }) => {
  switch (status) {
    case 'PENDING_MANAGER':
      return (
        <span className="badge badge-pending-manager">
          <Clock size={12} /> Pending Manager
        </span>
      );
    case 'PENDING_HR':
      return (
        <div style={{ display: 'inline-flex', gap: '0.35rem', alignItems: 'center' }}>
          <span className="badge badge-pending-hr">
            <Clock size={12} /> Pending HR
          </span>
          {escalated && (
            <span className="badge badge-escalated" title="Auto-escalated due to manager timeout">
              <AlertCircle size={12} /> Escalated
            </span>
          )}
        </div>
      );
    case 'APPROVED':
      return (
        <span className="badge badge-approved">
          <CheckCircle2 size={12} /> Approved
        </span>
      );
    case 'REJECTED':
      return (
        <span className="badge badge-rejected">
          <XCircle size={12} /> Rejected
        </span>
      );
    case 'CANCELLED':
      return (
        <span className="badge badge-cancelled">
          <Ban size={12} /> Cancelled
        </span>
      );
    default:
      return <span className="badge">{status}</span>;
  }
};

export default StatusBadge;
