import React from 'react';
import { Container, Typography, Box, Card, CardContent, Grid, Chip, Button } from '@mui/material';
import { Loyalty as LoyaltyIcon, Star, Redeem } from '@mui/icons-material';

const Loyalty: React.FC = () => {
  return (
    <Container maxWidth="lg">
      <Typography variant="h4" component="h1" gutterBottom sx={{ textAlign: 'center', mb: 4 }}>
        🏆 Loyalty Program
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <LoyaltyIcon color="primary" sx={{ fontSize: 60, mb: 2 }} />
              <Typography variant="h4" color="primary">
                5,000
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Total Points
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Star color="warning" sx={{ fontSize: 60, mb: 2 }} />
              <Typography variant="h4" color="warning.main">
                SILVER
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Current Tier
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Redeem color="success" sx={{ fontSize: 60, mb: 2 }} />
              <Typography variant="h4" color="success.main">
                12
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Rewards Redeemed
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Loyalty;
