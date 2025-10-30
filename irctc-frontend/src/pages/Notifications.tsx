import React, { useEffect, useState } from 'react';
import { Card, CardContent, Typography, List, ListItem, ListItemText, Chip, Stack, Alert, CircularProgress } from '@mui/material';
import { useAuth } from '../contexts/AuthContext';

interface NotificationItem {
  id: number;
  userId: number;
  type: string;
  subject: string;
  message: string;
  sentTime: string;
  status?: string;
}

const Notifications: React.FC = () => {
  const { user } = useAuth();
  const [items, setItems] = useState<NotificationItem[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const run = async () => {
      if (!user) return;
      setLoading(true);
      setError(null);
      try {
        const res = await fetch(`/api/notifications/user/${user.id}/recent?limit=20`);
        if (!res.ok) throw new Error(`Request failed with status ${res.status}`);
        const data = await res.json();
        setItems(Array.isArray(data) ? data : []);
      } catch (e: any) {
        setError(e.message || 'Failed to load notifications');
      } finally {
        setLoading(false);
      }
    };
    run();
  }, [user]);

  return (
    <Stack spacing={2}>
      <Typography variant="h2">Notifications</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      <Card>
        <CardContent>
          {loading && <CircularProgress />}
          {!loading && (!items || items.length === 0) && (
            <Typography color="text.secondary">No notifications.</Typography>
          )}
          {!loading && items && items.length > 0 && (
            <List>
              {items.map((n) => (
                <ListItem key={n.id} divider alignItems="flex-start">
                  <ListItemText
                    primary={
                      <Stack direction="row" spacing={1} alignItems="center">
                        <Typography variant="h6">{n.subject}</Typography>
                        <Chip size="small" label={n.type} />
                        {n.status ? <Chip size="small" label={n.status} color="success" /> : null}
                      </Stack>
                    }
                    secondary={
                      <>
                        <Typography variant="body2" color="text.secondary">
                          {new Date(n.sentTime).toLocaleString()}
                        </Typography>
                        <Typography variant="body1">{n.message}</Typography>
                      </>
                    }
                  />
                </ListItem>
              ))}
            </List>
          )}
        </CardContent>
      </Card>
    </Stack>
  );
};

export default Notifications;


