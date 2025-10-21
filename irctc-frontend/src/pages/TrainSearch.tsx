import React, { useState } from 'react';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  Chip,
  Alert,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import {
  Search,
  Train,
  Schedule,
  AttachMoney,
  CheckCircle,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useBooking } from '../contexts/BookingContext';
import AdvancedSearch from '../components/AdvancedSearch';
import FareComparison from '../components/FareComparison';
import axios from 'axios';

const TrainSearch: React.FC = () => {
  const [searchForm, setSearchForm] = useState({
    sourceStationCode: 'NDLS',
    destinationStationCode: 'MUMB',
    journeyDate: new Date().toISOString().split('T')[0],
    preferredClass: 'AC2',
    numberOfPassengers: 1,
  });
  const [trains, setTrains] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showAdvancedSearch, setShowAdvancedSearch] = useState(false);
  const { setSearchParams, setSelectedTrain } = useBooking();
  const navigate = useNavigate();

  const stations = [
    { code: 'NDLS', name: 'New Delhi' },
    { code: 'MUMB', name: 'Mumbai Central' },
    { code: 'AGC', name: 'Agra Cantt' },
    { code: 'BLR', name: 'Bangalore City' },
    { code: 'CHN', name: 'Chennai Central' },
    { code: 'KOL', name: 'Kolkata' },
    { code: 'HYD', name: 'Hyderabad' },
    { code: 'PUNE', name: 'Pune' },
  ];

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchForm({
      ...searchForm,
      [e.target.name]: e.target.value,
    });
  };

  const handleSelectChange = (e: any) => {
    setSearchForm({
      ...searchForm,
      [e.target.name]: e.target.value,
    });
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await axios.post('http://localhost:8082/api/trains/search', searchForm);
      setTrains(response.data);
      setSearchParams(searchForm);
    } catch (err: any) {
      setError('Failed to search trains. Please try again.');
      console.error('Search error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectTrain = (train: any) => {
    setSelectedTrain(train);
    navigate('/seats');
  };

  const handleAdvancedSearch = (filters: any) => {
    console.log('Advanced search filters:', filters);
    // Implement advanced search logic here
    handleSearch(new Event('submit') as any);
  };

  const handleSelectTrainFromComparison = (trainId: string, className: string) => {
    const train = trains.find(t => t.id === trainId);
    if (train) {
      setSelectedTrain(train);
      navigate('/seats');
    }
  };

  const formatTime = (time: string) => {
    return new Date(`2000-01-01T${time}`).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
    }).format(amount);
  };

  return (
    <Container maxWidth="lg">
      <Typography variant="h4" component="h1" gutterBottom sx={{ textAlign: 'center', mb: 4 }}>
        🚂 Search Trains
      </Typography>

      {/* Advanced Search Component */}
      <AdvancedSearch onSearch={handleAdvancedSearch} />

      <Paper elevation={3} sx={{ p: 3, mb: 4 }}>
        <Box component="form" onSubmit={handleSearch}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={6} md={2}>
              <FormControl fullWidth>
                <InputLabel>From</InputLabel>
                <Select
                  name="sourceStationCode"
                  value={searchForm.sourceStationCode}
                  onChange={handleSelectChange}
                  label="From"
                >
                  {stations.map((station) => (
                    <MenuItem key={station.code} value={station.code}>
                      {station.name} ({station.code})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <FormControl fullWidth>
                <InputLabel>To</InputLabel>
                <Select
                  name="destinationStationCode"
                  value={searchForm.destinationStationCode}
                  onChange={handleSelectChange}
                  label="To"
                >
                  {stations.map((station) => (
                    <MenuItem key={station.code} value={station.code}>
                      {station.name} ({station.code})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <TextField
                fullWidth
                name="journeyDate"
                label="Journey Date"
                type="date"
                value={searchForm.journeyDate}
                onChange={handleChange}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <FormControl fullWidth>
                <InputLabel>Class</InputLabel>
                <Select
                  name="preferredClass"
                  value={searchForm.preferredClass}
                  onChange={handleSelectChange}
                  label="Class"
                >
                  <MenuItem value="AC1">AC 1st Class</MenuItem>
                  <MenuItem value="AC2">AC 2nd Class</MenuItem>
                  <MenuItem value="AC3">AC 3rd Class</MenuItem>
                  <MenuItem value="SL">Sleeper</MenuItem>
                  <MenuItem value="CC">Chair Car</MenuItem>
                  <MenuItem value="GENERAL">General</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <TextField
                fullWidth
                name="numberOfPassengers"
                label="Passengers"
                type="number"
                value={searchForm.numberOfPassengers}
                onChange={handleChange}
                inputProps={{ min: 1, max: 6 }}
              />
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <Button
                type="submit"
                fullWidth
                variant="contained"
                size="large"
                startIcon={<Search />}
                disabled={loading}
              >
                {loading ? <CircularProgress size={24} /> : 'Search'}
              </Button>
            </Grid>
          </Grid>
        </Box>
      </Paper>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {trains.length > 0 && (
        <>
          <Typography variant="h5" gutterBottom>
            Available Trains ({trains.length})
          </Typography>
          
          {/* Fare Comparison Component */}
          <FareComparison 
            trains={trains} 
            onSelectTrain={handleSelectTrainFromComparison} 
          />
        </>
      )}

      <Grid container spacing={2}>
        {trains.map((train) => (
          <Grid item xs={12} key={train.id}>
            <Card
              sx={{
                transition: 'transform 0.2s',
                '&:hover': {
                  transform: 'translateY(-2px)',
                  boxShadow: 4,
                },
              }}
            >
              <CardContent>
                <Grid container spacing={2} alignItems="center">
                  <Grid item xs={12} md={3}>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <Train color="primary" sx={{ mr: 1 }} />
                      <Typography variant="h6" component="div">
                        {train.trainNumber}
                      </Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary">
                      {train.trainName}
                    </Typography>
                    <Chip
                      label={train.trainType}
                      size="small"
                      color="primary"
                      sx={{ mt: 1 }}
                    />
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <Schedule color="action" sx={{ mr: 1 }} />
                      <Typography variant="body1">
                        {formatTime(train.departureTime)} - {formatTime(train.arrivalTime)}
                      </Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary">
                      {train.journeyDuration} • {train.totalDistance} km
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {train.sourceStationName} → {train.destinationStationName}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} md={2}>
                    <Box sx={{ textAlign: 'center' }}>
                      <Typography variant="h6" color="primary">
                        {formatCurrency(train.startingFare)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Starting from
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} md={3}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <CheckCircle color="success" fontSize="small" />
                        <Typography variant="body2">
                          {train.availableSeats} seats available
                        </Typography>
                      </Box>
                      {train.isTatkalAvailable && (
                        <Chip label="Tatkal Available" size="small" color="warning" />
                      )}
                      <Button
                        variant="contained"
                        fullWidth
                        onClick={() => handleSelectTrain(train)}
                        startIcon={<AttachMoney />}
                      >
                        Book Now
                      </Button>
                    </Box>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {trains.length === 0 && !loading && (
        <Box sx={{ textAlign: 'center', py: 4 }}>
          <Typography variant="h6" color="text.secondary">
            No trains found. Please try different search criteria.
          </Typography>
        </Box>
      )}
    </Container>
  );
};

export default TrainSearch;
