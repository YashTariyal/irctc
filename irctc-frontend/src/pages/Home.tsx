import React from 'react';
import {
  Container,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  Button,
  Paper,
  Chip,
} from '@mui/material';
import {
  Train,
  Search,
  BookOnline,
  Dashboard,
  Security,
  Restaurant,
  Route,
  TrendingUp,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Home: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const features = [
    {
      title: 'Train Search & Booking',
      description: 'Search trains, select seats, and book tickets easily',
      icon: <Search />,
      color: '#1976d2',
      path: '/search',
    },
    {
      title: 'Seat Selection',
      description: 'Interactive seat map with real-time availability',
      icon: <BookOnline />,
      color: '#2e7d32',
      path: '/seats',
    },
    {
      title: 'Loyalty Program',
      description: 'Earn points and redeem rewards for your travels',
      icon: <TrendingUp />,
      color: '#ed6c02',
      path: '/loyalty',
    },
    {
      title: 'Travel Insurance',
      description: 'Comprehensive travel insurance for your journey',
      icon: <Security />,
      color: '#9c27b0',
      path: '/insurance',
    },
    {
      title: 'Meal Booking',
      description: 'Order meals from station vendors',
      icon: <Restaurant />,
      color: '#d32f2f',
      path: '/meals',
    },
    {
      title: 'Trip Planner',
      description: 'Plan multi-city journeys with connections',
      icon: <Route />,
      color: '#00695c',
      path: '/trip-planner',
    },
  ];

  const stats = [
    { label: 'Active Trains', value: '2,500+' },
    { label: 'Daily Bookings', value: '50,000+' },
    { label: 'Happy Customers', value: '1M+' },
    { label: 'Routes Covered', value: '7,000+' },
  ];

  return (
    <Container maxWidth="lg">
      {/* Hero Section */}
      <Box
        sx={{
          background: 'linear-gradient(135deg, #1976d2 0%, #42a5f5 100%)',
          color: 'white',
          borderRadius: 3,
          p: 6,
          mb: 4,
          textAlign: 'center',
        }}
      >
        <Typography variant="h2" component="h1" gutterBottom>
          🚂 Welcome to IRCTC Railway Booking
        </Typography>
        <Typography variant="h5" sx={{ mb: 4, opacity: 0.9 }}>
          Book train tickets, manage your journey, and explore India by rail
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
          <Button
            variant="contained"
            size="large"
            onClick={() => navigate('/search')}
            sx={{
              backgroundColor: 'white',
              color: '#1976d2',
              '&:hover': { backgroundColor: '#f5f5f5' },
            }}
          >
            Search Trains
          </Button>
          {!user && (
            <Button
              variant="outlined"
              size="large"
              onClick={() => navigate('/register')}
              sx={{ borderColor: 'white', color: 'white' }}
            >
              Create Account
            </Button>
          )}
        </Box>
      </Box>

      {/* Stats Section */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {stats.map((stat, index) => (
          <Grid item xs={6} md={3} key={index}>
            <Paper
              sx={{
                p: 3,
                textAlign: 'center',
                background: 'linear-gradient(45deg, #f5f5f5 30%, #e0e0e0 90%)',
              }}
            >
              <Typography variant="h4" color="primary" fontWeight="bold">
                {stat.value}
              </Typography>
              <Typography variant="body1" color="text.secondary">
                {stat.label}
              </Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>

      {/* Features Section */}
      <Typography variant="h3" component="h2" gutterBottom sx={{ textAlign: 'center', mb: 4 }}>
        Our Features
      </Typography>
      <Grid container spacing={3}>
        {features.map((feature, index) => (
          <Grid item xs={12} sm={6} md={4} key={index}>
            <Card
              sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                transition: 'transform 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 6,
                },
              }}
            >
              <CardContent sx={{ flexGrow: 1 }}>
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    mb: 2,
                  }}
                >
                  <Box
                    sx={{
                      backgroundColor: feature.color,
                      color: 'white',
                      borderRadius: 2,
                      p: 1.5,
                      mr: 2,
                    }}
                  >
                    {feature.icon}
                  </Box>
                  <Typography variant="h6" component="h3">
                    {feature.title}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  {feature.description}
                </Typography>
                <Button
                  variant="outlined"
                  fullWidth
                  onClick={() => navigate(feature.path)}
                  sx={{ borderColor: feature.color, color: feature.color }}
                >
                  Explore
                </Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Quick Actions */}
      {user && (
        <Box sx={{ mt: 6, p: 3, backgroundColor: '#f8f9fa', borderRadius: 2 }}>
          <Typography variant="h4" gutterBottom sx={{ textAlign: 'center' }}>
            Quick Actions
          </Typography>
          <Grid container spacing={2} justifyContent="center">
            <Grid item>
              <Button
                variant="contained"
                startIcon={<Search />}
                onClick={() => navigate('/search')}
                size="large"
              >
                Search Trains
              </Button>
            </Grid>
            <Grid item>
              <Button
                variant="contained"
                startIcon={<BookOnline />}
                onClick={() => navigate('/my-bookings')}
                size="large"
              >
                My Bookings
              </Button>
            </Grid>
            <Grid item>
              <Button
                variant="contained"
                startIcon={<Dashboard />}
                onClick={() => navigate('/dashboard')}
                size="large"
              >
                Dashboard
              </Button>
            </Grid>
          </Grid>
        </Box>
      )}

      {/* Footer */}
      <Box sx={{ mt: 6, p: 3, textAlign: 'center', backgroundColor: '#f5f5f5', borderRadius: 2 }}>
        <Typography variant="h6" gutterBottom>
          🚂 IRCTC Railway Booking System
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Built with React, TypeScript, Material-UI, and Spring Boot
        </Typography>
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'center', gap: 1, flexWrap: 'wrap' }}>
          <Chip label="React" color="primary" size="small" />
          <Chip label="TypeScript" color="primary" size="small" />
          <Chip label="Material-UI" color="primary" size="small" />
          <Chip label="Spring Boot" color="secondary" size="small" />
          <Chip label="JWT Auth" color="secondary" size="small" />
          <Chip label="REST API" color="secondary" size="small" />
        </Box>
      </Box>
    </Container>
  );
};

export default Home;
