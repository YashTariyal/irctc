import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  Button,
  Alert,
  CircularProgress,
  Chip,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useBooking } from '../contexts/BookingContext';
import axios from 'axios';

const SeatSelection: React.FC = () => {
  const { selectedTrain, setSelectedSeats } = useBooking();
  const [seats, setSeats] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedSeatIds, setSelectedSeatIds] = useState<number[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    if (!selectedTrain) {
      navigate('/search');
      return;
    }
    fetchSeats();
  }, [selectedTrain, navigate]);

  const fetchSeats = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await axios.get(
        `http://localhost:8082/api/seat-selection/trains/${selectedTrain?.id}/coaches/1/seats`,
        {
          params: {
            journeyDate: new Date().toISOString().split('T')[0],
          },
        }
      );
      setSeats(response.data.availableSeats || []);
    } catch (err: any) {
      setError('Failed to load seats. Please try again.');
      console.error('Seats error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSeatClick = (seatId: number, isAvailable: boolean) => {
    if (!isAvailable) return;

    setSelectedSeatIds((prev) => {
      if (prev.includes(seatId)) {
        return prev.filter((id) => id !== seatId);
      } else {
        return [...prev, seatId];
      }
    });
  };

  const handleProceed = () => {
    const selectedSeatsData = seats.filter((seat) => selectedSeatIds.includes(seat.id));
    setSelectedSeats(selectedSeatsData);
    navigate('/booking');
  };

  const getSeatColor = (seat: any) => {
    if (!seat.isAvailable) return '#f44336';
    if (selectedSeatIds.includes(seat.id)) return '#4caf50';
    if (seat.isLadiesQuota) return '#e91e63';
    if (seat.isSeniorCitizenQuota) return '#ff9800';
    if (seat.isHandicappedFriendly) return '#9c27b0';
    return '#2196f3';
  };

  const getSeatLabel = (seat: any) => {
    if (!seat.isAvailable) return 'X';
    if (selectedSeatIds.includes(seat.id)) return '✓';
    return seat.seatNumber;
  };

  if (!selectedTrain) {
    return (
      <Container>
        <Alert severity="warning">Please select a train first.</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Typography variant="h4" component="h1" gutterBottom sx={{ textAlign: 'center', mb: 4 }}>
        🪑 Select Your Seats
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={6}>
              <Typography variant="h6">
                {selectedTrain.trainNumber} - {selectedTrain.trainName}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {selectedTrain.sourceStationName} → {selectedTrain.destinationStationName}
              </Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                <Chip label="Available" color="primary" size="small" />
                <Chip label="Selected" color="success" size="small" />
                <Chip label="Ladies Quota" sx={{ backgroundColor: '#e91e63', color: 'white' }} size="small" />
                <Chip label="Senior Citizen" sx={{ backgroundColor: '#ff9800', color: 'white' }} size="small" />
                <Chip label="Handicapped" sx={{ backgroundColor: '#9c27b0', color: 'white' }} size="small" />
                <Chip label="Occupied" color="error" size="small" />
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          <Box sx={{ mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              Coach Layout - AC2 (A1)
            </Typography>
            <Box
              sx={{
                display: 'grid',
                gridTemplateColumns: 'repeat(8, 1fr)',
                gap: 1,
                maxWidth: '800px',
                mx: 'auto',
              }}
            >
              {seats.map((seat) => (
                <Button
                  key={seat.id}
                  variant={selectedSeatIds.includes(seat.id) ? 'contained' : 'outlined'}
                  onClick={() => handleSeatClick(seat.id, seat.isAvailable)}
                  disabled={!seat.isAvailable}
                  sx={{
                    minWidth: '60px',
                    height: '40px',
                    backgroundColor: getSeatColor(seat),
                    color: 'white',
                    '&:hover': {
                      backgroundColor: getSeatColor(seat),
                      opacity: 0.8,
                    },
                    '&:disabled': {
                      backgroundColor: '#f44336',
                      color: 'white',
                    },
                  }}
                >
                  {getSeatLabel(seat)}
                </Button>
              ))}
            </Box>
          </Box>

          {selectedSeatIds.length > 0 && (
            <Box sx={{ textAlign: 'center', mt: 4 }}>
              <Typography variant="h6" gutterBottom>
                Selected Seats: {selectedSeatIds.length}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Total Fare: ₹{selectedSeatIds.length * 2500}
              </Typography>
              <Button
                variant="contained"
                size="large"
                onClick={handleProceed}
                sx={{ minWidth: '200px' }}
              >
                Proceed to Booking
              </Button>
            </Box>
          )}
        </>
      )}
    </Container>
  );
};

export default SeatSelection;
