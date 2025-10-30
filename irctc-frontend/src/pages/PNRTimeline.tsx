import React, { useState } from 'react';
import { Card, CardContent, Typography, TextField, Button, Stack, Alert, CircularProgress, List, ListItem, ListItemText } from '@mui/material';

interface TimelineEvent {
  status: string;
  timestamp: string;
  description?: string;
}

const PNRTimeline: React.FC = () => {
  const [pnr, setPnr] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [events, setEvents] = useState<TimelineEvent[] | null>(null);

  const fetchTimeline = async () => {
    if (!pnr.trim()) {
      setError('Please enter a PNR number');
      return;
    }
    setLoading(true);
    setError(null);
    setEvents(null);
    try {
      const res = await fetch(`/api/pnr/${encodeURIComponent(pnr.trim())}/timeline`);
      if (!res.ok) {
        throw new Error(`Request failed with status ${res.status}`);
      }
      const data = await res.json();
      setEvents(Array.isArray(data) ? data : data?.events || []);
    } catch (e: any) {
      setError(e.message || 'Failed to fetch PNR timeline');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Stack spacing={2}>
      <Typography variant="h2">PNR Timeline</Typography>
      <Card>
        <CardContent>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ sm: 'center' }}>
            <TextField
              label="PNR Number"
              value={pnr}
              onChange={(e) => setPnr(e.target.value)}
              placeholder="e.g., 1234567890"
              size="small"
            />
            <Button variant="contained" onClick={fetchTimeline} disabled={loading}>
              {loading ? <CircularProgress size={20} color="inherit" /> : 'Fetch Timeline'}
            </Button>
          </Stack>
          {error && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {error}
            </Alert>
          )}
        </CardContent>
      </Card>

      {events && (
        <Card>
          <CardContent>
            <Typography variant="h3" gutterBottom>
              Events
            </Typography>
            {events.length === 0 ? (
              <Typography color="text.secondary">No timeline events found.</Typography>
            ) : (
              <List>
                {events.map((ev, idx) => (
                  <ListItem key={`${ev.status}-${idx}`} divider>
                    <ListItemText
                      primary={`${ev.status}`}
                      secondary={
                        <>
                          <Typography component="span" variant="body2" color="text.secondary">
                            {new Date(ev.timestamp).toLocaleString()}
                          </Typography>
                          {ev.description ? (
                            <Typography component="div" variant="body2">{ev.description}</Typography>
                          ) : null}
                        </>
                      }
                    />
                  </ListItem>
                ))}
              </List>
            )}
          </CardContent>
        </Card>
      )}
    </Stack>
  );
};

export default PNRTimeline;


