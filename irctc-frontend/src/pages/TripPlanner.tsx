import React from 'react';
import { Container, Typography, Box, Card, CardContent, Grid, Button } from '@mui/material';
import { Route, Train, Schedule } from '@mui/icons-material';

const TripPlanner: React.FC = () => {
  return (
    <Container maxWidth="lg">
      <Typography variant="h4" component="h1" gutterBottom sx={{ textAlign: 'center', mb: 4 }}>
        🗺️ Trip Planner
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Route color="primary" sx={{ mr: 2 }} />
                <Typography variant="h6">Multi-City Journey</Typography>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Plan your journey with multiple stops
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Train color="action" sx={{ mr: 1 }} />
                <Typography variant="body2">New Delhi → Agra → Mumbai</Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Schedule color="action" sx={{ mr: 1 }} />
                <Typography variant="body2">Total Duration: 20 hours</Typography>
              </Box>
              <Button variant="contained" fullWidth>
                Plan Journey
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default TripPlanner;
