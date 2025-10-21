import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  Button,
  Alert,
  CircularProgress,
  Grid,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useBooking } from '../contexts/BookingContext';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';

const Booking: React.FC = () => {
  const { selectedTrain, selectedSeats, setBookingData } = useBooking();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [passengerData, setPassengerData] = useState({
    firstName: '',
    lastName: '',
    age: '',
    gender: '',
    idProofType: '',
    idProofNumber: '',
  });
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPassengerData({
      ...passengerData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSelectChange = (e: any) => {
    setPassengerData({
      ...passengerData,
      [e.target.name]: e.target.value,
    });
  };

  const handleBooking = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // First create passenger
      const passengerResponse = await axios.post('http://localhost:8082/api/passengers', {
        firstName: passengerData.firstName,
        lastName: passengerData.lastName,
        age: parseInt(passengerData.age),
        gender: passengerData.gender,
        idProofType: passengerData.idProofType,
        idProofNumber: passengerData.idProofNumber,
      });

      // Then create booking
      const bookingResponse = await axios.post('http://localhost:8082/api/bookings', {
        trainId: selectedTrain?.id,
        passengerId: passengerResponse.data.id,
        seatId: selectedSeats[0]?.id,
        coachId: 1,
        journeyDate: new Date().toISOString().split('T')[0],
        totalFare: selectedSeats.length * 2500,
        baseFare: selectedSeats.length * 2000,
        quotaType: 'GENERAL',
        isTatkal: false,
      });

      setBookingData(bookingResponse.data);
      setSuccess('Booking successful! Redirecting to payment...');
      
      setTimeout(() => {
        navigate('/my-bookings');
      }, 2000);

    } catch (err: any) {
      setError('Booking failed. Please try again.');
      console.error('Booking error:', err);
    } finally {
      setLoading(false);
    }
  };

  if (!selectedTrain || !selectedSeats.length) {
    return (
      <Container>
        <Alert severity="warning">Please select a train and seats first.</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="md">
      <Typography variant="h4" component="h1" gutterBottom sx={{ textAlign: 'center', mb: 4 }}>
        🎫 Complete Your Booking
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Passenger Details
              </Typography>
              <Box component="form" onSubmit={handleBooking}>
                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      required
                      fullWidth
                      name="firstName"
                      label="First Name"
                      value={passengerData.firstName}
                      onChange={handleChange}
                      disabled={loading}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      required
                      fullWidth
                      name="lastName"
                      label="Last Name"
                      value={passengerData.lastName}
                      onChange={handleChange}
                      disabled={loading}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      required
                      fullWidth
                      name="age"
                      label="Age"
                      type="number"
                      value={passengerData.age}
                      onChange={handleChange}
                      disabled={loading}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <FormControl fullWidth required>
                      <InputLabel>Gender</InputLabel>
                      <Select
                        name="gender"
                        value={passengerData.gender}
                        onChange={handleSelectChange}
                        label="Gender"
                        disabled={loading}
                      >
                        <MenuItem value="MALE">Male</MenuItem>
                        <MenuItem value="FEMALE">Female</MenuItem>
                        <MenuItem value="OTHER">Other</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <FormControl fullWidth required>
                      <InputLabel>ID Proof Type</InputLabel>
                      <Select
                        name="idProofType"
                        value={passengerData.idProofType}
                        onChange={handleSelectChange}
                        label="ID Proof Type"
                        disabled={loading}
                      >
                        <MenuItem value="AADHAR">Aadhar Card</MenuItem>
                        <MenuItem value="PAN">PAN Card</MenuItem>
                        <MenuItem value="PASSPORT">Passport</MenuItem>
                        <MenuItem value="DRIVING_LICENSE">Driving License</MenuItem>
                        <MenuItem value="VOTER_ID">Voter ID</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      required
                      fullWidth
                      name="idProofNumber"
                      label="ID Proof Number"
                      value={passengerData.idProofNumber}
                      onChange={handleChange}
                      disabled={loading}
                    />
                  </Grid>
                </Grid>
                <Box sx={{ mt: 3, textAlign: 'center' }}>
                  <Button
                    type="submit"
                    variant="contained"
                    size="large"
                    disabled={loading}
                    sx={{ minWidth: '200px' }}
                  >
                    {loading ? <CircularProgress size={24} /> : 'Confirm Booking'}
                  </Button>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Booking Summary
              </Typography>
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Train: {selectedTrain.trainNumber}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {selectedTrain.trainName}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {selectedTrain.sourceStationName} → {selectedTrain.destinationStationName}
                </Typography>
              </Box>
              
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Selected Seats: {selectedSeats.length}
                </Typography>
                {selectedSeats.map((seat, index) => (
                  <Typography key={index} variant="body2" color="text.secondary">
                    {seat.seatNumber} - ₹{seat.fare}
                  </Typography>
                ))}
              </Box>

              <Box sx={{ borderTop: 1, borderColor: 'divider', pt: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2">Base Fare:</Typography>
                  <Typography variant="body2">₹{selectedSeats.length * 2000}</Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2">Taxes:</Typography>
                  <Typography variant="body2">₹{selectedSeats.length * 500}</Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', fontWeight: 'bold' }}>
                  <Typography variant="h6">Total:</Typography>
                  <Typography variant="h6">₹{selectedSeats.length * 2500}</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {error && (
        <Alert severity="error" sx={{ mt: 2 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mt: 2 }}>
          {success}
        </Alert>
      )}
    </Container>
  );
};

export default Booking;
