import React from 'react';
import { Container, Typography, Box, Card, CardContent, Grid, Button } from '@mui/material';
import { Restaurant, Menu, ShoppingCart } from '@mui/icons-material';

const MealBooking: React.FC = () => {
  return (
    <Container maxWidth="lg">
      <Typography variant="h4" component="h1" gutterBottom sx={{ textAlign: 'center', mb: 4 }}>
        🍽️ Meal Booking
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Restaurant color="primary" sx={{ mr: 2 }} />
                <Typography variant="h6">Railway Catering Services</Typography>
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Fresh meals delivered to your seat
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Menu color="action" sx={{ mr: 1 }} />
                <Typography variant="body2">Vegetable Biryani - ₹150</Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Menu color="action" sx={{ mr: 1 }} />
                <Typography variant="body2">Chicken Curry - ₹200</Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Menu color="action" sx={{ mr: 1 }} />
                <Typography variant="body2">Masala Chai - ₹20</Typography>
              </Box>
              <Button variant="contained" startIcon={<ShoppingCart />} fullWidth>
                Order Now
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default MealBooking;
