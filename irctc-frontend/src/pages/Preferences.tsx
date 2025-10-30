import React, { useEffect, useState } from 'react';

interface Preferences {
  userId: number;
  emailEnabled: boolean;
  smsEnabled: boolean;
  pushEnabled: boolean;
  quietHours?: string;
}

const PreferencesPage: React.FC = () => {
  const [prefs, setPrefs] = useState<Preferences | null>(null);
  const userId = 1; // TODO: wire with auth context when available

  useEffect(() => {
    fetch(`/api/users/${userId}/preferences`)
      .then((r) => r.json())
      .then(setPrefs)
      .catch(() => setPrefs({ userId, emailEnabled: true, smsEnabled: false, pushEnabled: true } as Preferences));
  }, [userId]);

  const save = async () => {
    if (!prefs) return;
    await fetch(`/api/users/${userId}/preferences`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(prefs),
    });
    alert('Preferences saved');
  };

  if (!prefs) return <div style={{ padding: 24 }}>Loading...</div>;

  return (
    <div style={{ maxWidth: 560, margin: '24px auto', padding: 24, background: '#fff', borderRadius: 8, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}>
      <h2 style={{ marginTop: 0 }}>Notification Preferences</h2>
      <p>Control how you receive updates like ticket confirmations and reminders.</p>

      <div style={{ display: 'grid', gap: 16 }}>
        <label style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <input type="checkbox" checked={prefs.emailEnabled} onChange={(e) => setPrefs({ ...prefs, emailEnabled: e.target.checked })} />
          Email Notifications
        </label>
        <label style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <input type="checkbox" checked={prefs.smsEnabled} onChange={(e) => setPrefs({ ...prefs, smsEnabled: e.target.checked })} />
          SMS Notifications
        </label>
        <label style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <input type="checkbox" checked={prefs.pushEnabled} onChange={(e) => setPrefs({ ...prefs, pushEnabled: e.target.checked })} />
          Push Notifications
        </label>
        <label style={{ display: 'grid', gap: 6 }}>
          Quiet Hours (e.g. 22:00-07:00)
          <input
            type="text"
            value={prefs.quietHours || ''}
            onChange={(e) => setPrefs({ ...prefs, quietHours: e.target.value })}
            placeholder="22:00-07:00"
            style={{ padding: 8, borderRadius: 6, border: '1px solid #ddd' }}
          />
        </label>
      </div>

      <div style={{ marginTop: 24 }}>
        <button onClick={save} style={{ padding: '10px 16px', borderRadius: 6, background: '#1976d2', color: '#fff', border: 'none' }}>
          Save Preferences
        </button>
      </div>
    </div>
  );
};

export default PreferencesPage;
