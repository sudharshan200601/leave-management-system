import React, { useState } from 'react';
import { AlertTriangle, ShieldCheck, Info } from 'lucide-react';

const ConflictBadge = ({ flag, severity, details }) => {
  const [showTooltip, setShowTooltip] = useState(false);

  if (!flag || severity === 'NONE') {
    return (
      <span className="badge" style={{ background: 'rgba(255, 255, 255, 0.05)', color: 'var(--text-muted)' }}>
        <ShieldCheck size={12} /> Clear
      </span>
    );
  }

  const isHigh = severity === 'HIGH';
  const badgeClass = isHigh ? 'badge badge-conflict-high' : 'badge badge-conflict-low';

  return (
    <div style={{ position: 'relative', display: 'inline-block' }}>
      <span
        className={badgeClass}
        style={{ cursor: 'pointer' }}
        onClick={() => setShowTooltip(!showTooltip)}
        onMouseEnter={() => setShowTooltip(true)}
        onMouseLeave={() => setShowTooltip(false)}
      >
        <AlertTriangle size={12} /> {severity} Overload <Info size={11} style={{ opacity: 0.8 }} />
      </span>

      {showTooltip && details && (
        <div
          style={{
            position: 'absolute',
            bottom: '125%',
            left: '50%',
            transform: 'translateX(-50%)',
            width: '240px',
            backgroundColor: '#1f2937',
            border: '1px solid var(--border-color)',
            borderRadius: 'var(--radius-md)',
            padding: '0.65rem 0.85rem',
            boxShadow: 'var(--shadow-lg)',
            zIndex: 100,
            fontSize: '0.8rem',
            color: '#f3f4f6',
            pointerEvents: 'none',
          }}
        >
          <div style={{ fontWeight: 700, marginBottom: '0.2rem', color: isHigh ? '#f87171' : '#fbbf24' }}>
            {severity} Team Absence Risk
          </div>
          <div>{details}</div>
        </div>
      )}
    </div>
  );
};

export default ConflictBadge;
