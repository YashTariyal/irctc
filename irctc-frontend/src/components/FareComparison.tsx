import React from 'react';
import {
  Card,
  CardContent,
  Typography,
  Grid,
  Box,
  Chip,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Divider,
  Avatar,
} from '@mui/material';
import {
  Train,
  Schedule,
  AttachMoney,
  Star,
  Wifi,
  Restaurant,
  LocalDrink,
  Bed,
  CompareArrows,
} from '@mui/icons-material';

interface TrainOption {
  id: string;
  trainNumber: string;
  trainName: string;
  departureTime: string;
  arrivalTime: string;
  duration: string;
  distance: number;
  classes: {
    name: string;
    price: number;
    availability: string;
    amenities: string[];
  }[];
  rating: number;
  onTimePercentage: number;
  features: string[];
}

interface FareComparisonProps {
  trains: TrainOption[];
  onSelectTrain: (trainId: string, className: string) => void;
}

const FareComparison: React.FC<FareComparisonProps> = ({ trains, onSelectTrain }) => {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
    }).format(amount);
  };

  const getAvailabilityColor = (availability: string) => {
    switch (availability.toLowerCase()) {
      case 'available':
        return 'success';
      case 'rac':
        return 'warning';
      case 'waitlist':
        return 'error';
      default:
        return 'default';
    }
  };

  const getAmenityIcon = (amenity: string) => {
    switch (amenity.toLowerCase()) {
      case 'wifi':
        return <Wifi fontSize="small" />;
      case 'food':
        return <Restaurant fontSize="small" />;
      case 'bedding':
        return <Bed fontSize="small" />;
      case 'water':
        return <LocalDrink fontSize="small" />;
      default:
        return null;
    }
  };

  return (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <CompareArrows sx={{ mr: 1, color: 'primary.main' }} />
          <Typography variant="h6" component="h2">
            Fare Comparison
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ ml: 2 }}>
            {trains.length} trains found
          </Typography>
        </Box>

        <TableContainer component={Paper} sx={{ maxHeight: 600 }}>
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell>Train Details</TableCell>
                <TableCell>Timing</TableCell>
                <TableCell>Duration</TableCell>
                <TableCell>Classes & Fare</TableCell>
                <TableCell>Rating</TableCell>
                <TableCell>Features</TableCell>
                <TableCell>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {trains.map((train) => (
                <TableRow key={train.id} hover>
                  {/* Train Details */}
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <Avatar sx={{ bgcolor: 'primary.main', mr: 1 }}>
                        <Train />
                      </Avatar>
                      <Box>
                        <Typography variant="subtitle2" fontWeight="bold">
                          {train.trainNumber}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {train.trainName}
                        </Typography>
                      </Box>
                    </Box>
                    <Typography variant="caption" color="text.secondary">
                      Distance: {train.distance} km
                    </Typography>
                  </TableCell>

                  {/* Timing */}
                  <TableCell>
                    <Box sx={{ textAlign: 'center' }}>
                      <Typography variant="body2" fontWeight="bold">
                        {train.departureTime}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Departure
                      </Typography>
                      <Divider sx={{ my: 1 }} />
                      <Typography variant="body2" fontWeight="bold">
                        {train.arrivalTime}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Arrival
                      </Typography>
                    </Box>
                  </TableCell>

                  {/* Duration */}
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <Schedule sx={{ mr: 1, fontSize: 16 }} />
                      <Typography variant="body2">
                        {train.duration}
                      </Typography>
                    </Box>
                    <Typography variant="caption" color="text.secondary">
                      On-time: {train.onTimePercentage}%
                    </Typography>
                  </TableCell>

                  {/* Classes & Fare */}
                  <TableCell>
                    <Box sx={{ maxWidth: 200 }}>
                      {train.classes.map((cls, index) => (
                        <Box key={index} sx={{ mb: 1, p: 1, border: '1px solid', borderColor: 'divider', borderRadius: 1 }}>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 0.5 }}>
                            <Typography variant="body2" fontWeight="bold">
                              {cls.name}
                            </Typography>
                            <Chip
                              label={cls.availability}
                              size="small"
                              color={getAvailabilityColor(cls.availability) as any}
                            />
                          </Box>
                          <Typography variant="h6" color="primary" fontWeight="bold">
                            {formatCurrency(cls.price)}
                          </Typography>
                          <Box sx={{ display: 'flex', gap: 0.5, mt: 0.5 }}>
                            {cls.amenities.map((amenity, idx) => (
                              <Box key={idx} sx={{ display: 'flex', alignItems: 'center' }}>
                                {getAmenityIcon(amenity)}
                              </Box>
                            ))}
                          </Box>
                        </Box>
                      ))}
                    </Box>
                  </TableCell>

                  {/* Rating */}
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <Star sx={{ color: 'warning.main', mr: 0.5 }} />
                      <Typography variant="body2" fontWeight="bold">
                        {train.rating}/5
                      </Typography>
                    </Box>
                    <Typography variant="caption" color="text.secondary">
                      Based on reviews
                    </Typography>
                  </TableCell>

                  {/* Features */}
                  <TableCell>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {train.features.map((feature, index) => (
                        <Chip
                          key={index}
                          label={feature}
                          size="small"
                          variant="outlined"
                        />
                      ))}
                    </Box>
                  </TableCell>

                  {/* Action */}
                  <TableCell>
                    <Button
                      variant="contained"
                      size="small"
                      onClick={() => onSelectTrain(train.id, train.classes[0].name)}
                    >
                      Book Now
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Summary */}
        <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
          <Typography variant="h6" gutterBottom>
            Quick Summary
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} md={3}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h4" color="primary" fontWeight="bold">
                  {trains.length}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Trains Available
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={12} md={3}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h4" color="success.main" fontWeight="bold">
                  {Math.min(...trains.map(t => t.classes[0].price))}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Lowest Fare (₹)
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={12} md={3}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h4" color="info.main" fontWeight="bold">
                  {Math.round(trains.reduce((acc, t) => acc + t.rating, 0) / trains.length * 10) / 10}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Average Rating
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={12} md={3}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h4" color="warning.main" fontWeight="bold">
                  {Math.round(trains.reduce((acc, t) => acc + t.onTimePercentage, 0) / trains.length)}%
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  On-time Performance
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </Box>
      </CardContent>
    </Card>
  );
};

export default FareComparison;
