import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Card,
  CardContent,
  Grid,
  Box,
  Button,
  Chip,
  Avatar,
  Divider,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  Train,
  Person,
  Schedule,
  AttachMoney,
  Receipt,
  Cancel,
  Download,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

interface Booking {
  id: string;
  trainNumber: string;
  trainName: string;
  fromStation: string;
  toStation: string;
  journeyDate: string;
  departureTime: string;
  arrivalTime: string;
  passengerName: string;
  passengerAge: number;
  passengerGender: string;
  seatNumber: string;
  class: string;
  fare: number;
  status: 'CONFIRMED' | 'CANCELLED' | 'PENDING';
  bookingDate: string;
  pnr: string;
}

const MyBookings: React.FC = () => {
  const { user } = useAuth();
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchBookings();
  }, []);

  const fetchBookings = async () => {
    try {
      setLoading(true);
      // Mock data for now - replace with actual API call
      const mockBookings: Booking[] = [
        {
          id: '1',
          trainNumber: '12345',
          trainName: 'Rajdhani Express',
          fromStation: 'Delhi',
          toStation: 'Mumbai',
          journeyDate: '2024-01-15',
          departureTime: '08:00',
          arrivalTime: '14:30',
          passengerName: 'John Doe',
          passengerAge: 30,
          passengerGender: 'Male',
          seatNumber: 'A1',
          class: 'AC First',
          fare: 2500,
          status: 'CONFIRMED',
          bookingDate: '2024-01-10',
          pnr: 'PNR123456',
        },
        {
          id: '2',
          trainNumber: '67890',
          trainName: 'Shatabdi Express',
          fromStation: 'Mumbai',
          toStation: 'Pune',
          journeyDate: '2024-01-20',
          departureTime: '06:00',
          arrivalTime: '09:30',
          passengerName: 'Jane Smith',
          passengerAge: 25,
          passengerGender: 'Female',
          seatNumber: 'B2',
          class: 'AC Chair Car',
          fare: 1200,
          status: 'PENDING',
          bookingDate: '2024-01-12',
          pnr: 'PNR789012',
        },
      ];
      setBookings(mockBookings);
    } catch (err) {
      setError('Failed to fetch bookings');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelBooking = async (bookingId: string) => {
    try {
      // Implement cancel booking logic
      console.log('Cancelling booking:', bookingId);
      // Update local state
      setBookings(bookings.filter(booking => booking.id !== bookingId));
    } catch (err) {
      console.error('Failed to cancel booking:', err);
    }
  };

  const handleDownloadTicket = (bookingId: string) => {
    // Implement download ticket logic
    console.log('Downloading ticket for booking:', bookingId);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CONFIRMED':
        return 'success';
      case 'CANCELLED':
        return 'error';
      case 'PENDING':
        return 'warning';
      default:
        return 'default';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatTime = (timeString: string) => {
    return timeString;
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
    }).format(amount);
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom sx={{ textAlign: 'center', mb: 4 }}>
        My Bookings
      </Typography>

      {bookings.length === 0 ? (
        <Card>
          <CardContent sx={{ textAlign: 'center', py: 8 }}>
            <Receipt sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
            <Typography variant="h6" color="text.secondary" gutterBottom>
              No bookings found
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Your train bookings will appear here once you make a reservation.
            </Typography>
          </CardContent>
        </Card>
      ) : (
        <Grid container spacing={3}>
          {bookings.map((booking) => (
            <Grid item xs={12} key={booking.id}>
              <Card>
                <CardContent>
                  <Grid container spacing={2} alignItems="center">
                    <Grid item xs={12} md={3}>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <Train color="primary" sx={{ mr: 1 }} />
                        <Typography variant="h6">
                          {booking.trainNumber}
                        </Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        {booking.trainName}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} md={2}>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <Person color="action" sx={{ mr: 1 }} />
                        <Typography variant="body1">
                          {booking.passengerName}
                        </Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        {booking.passengerAge} years, {booking.passengerGender}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} md={2}>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <Schedule color="action" sx={{ mr: 1 }} />
                        <Typography variant="body1">
                          {formatDate(booking.journeyDate)}
                        </Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        {formatTime(booking.departureTime)} - {formatTime(booking.arrivalTime)}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} md={2}>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <AttachMoney color="action" sx={{ mr: 1 }} />
                        <Typography variant="body1">
                          {formatCurrency(booking.fare)}
                        </Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        {booking.class} - {booking.seatNumber}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} md={3}>
                      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                        <Button
                          variant="outlined"
                          startIcon={<Download />}
                          size="small"
                          onClick={() => handleDownloadTicket(booking.id)}
                        >
                          Download
                        </Button>
                        {booking.status === 'CONFIRMED' && (
                          <Button
                            variant="outlined"
                            color="error"
                            startIcon={<Cancel />}
                            size="small"
                            onClick={() => handleCancelBooking(booking.id)}
                          >
                            Cancel
                          </Button>
                        )}
                      </Box>
                    </Grid>
                  </Grid>

                  <Divider sx={{ my: 2 }} />

                  <Grid container spacing={2}>
                    <Grid item xs={12} sm={6} md={3}>
                      <Typography variant="body2" color="text.secondary">
                        PNR Number
                      </Typography>
                      <Typography variant="body1" fontWeight="medium">
                        {booking.pnr}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={3}>
                      <Typography variant="body2" color="text.secondary">
                        Route
                      </Typography>
                      <Typography variant="body1" fontWeight="medium">
                        {booking.fromStation} → {booking.toStation}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={3}>
                      <Typography variant="body2" color="text.secondary">
                        Booking Date
                      </Typography>
                      <Typography variant="body1" fontWeight="medium">
                        {formatDate(booking.bookingDate)}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} sm={6} md={3}>
                      <Typography variant="body2" color="text.secondary">
                        Status
                      </Typography>
                      <Chip
                        label={booking.status}
                        color={getStatusColor(booking.status) as any}
                        size="small"
                      />
                    </Grid>
                  </Grid>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Container>
  );
};

export default MyBookings;