import React, { useState } from 'react';
import {
  Card,
  CardContent,
  Typography,
  Grid,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Box,
  Chip,
  Autocomplete,
  FormControlLabel,
  Switch,
  Slider,
  Paper,
} from '@mui/material';
import {
  Search,
  FilterList,
  Clear,
  Train,
  Schedule,
  AttachMoney,
} from '@mui/icons-material';

interface AdvancedSearchProps {
  onSearch: (filters: SearchFilters) => void;
}

interface SearchFilters {
  fromStation: string;
  toStation: string;
  journeyDate: string;
  returnDate?: string;
  trainType: string[];
  classType: string[];
  priceRange: [number, number];
  departureTime: string;
  arrivalTime: string;
  amenities: string[];
  flexibleDates: boolean;
}

const AdvancedSearch: React.FC<AdvancedSearchProps> = ({ onSearch }) => {
  const [filters, setFilters] = useState<SearchFilters>({
    fromStation: '',
    toStation: '',
    journeyDate: '',
    returnDate: '',
    trainType: [],
    classType: [],
    priceRange: [0, 5000],
    departureTime: '',
    arrivalTime: '',
    amenities: [],
    flexibleDates: false,
  });

  const [showAdvanced, setShowAdvanced] = useState(false);

  const stations = [
    'New Delhi (NDLS)',
    'Mumbai Central (BCT)',
    'Chennai Central (MAS)',
    'Kolkata (HWH)',
    'Bangalore (SBC)',
    'Hyderabad (HYB)',
    'Pune (PUNE)',
    'Ahmedabad (ADI)',
    'Jaipur (JP)',
    'Lucknow (LKO)',
  ];

  const trainTypes = [
    'Rajdhani Express',
    'Shatabdi Express',
    'Duronto Express',
    'Garib Rath',
    'Jan Shatabdi',
    'Sampark Kranti',
    'Superfast',
    'Express',
    'Passenger',
  ];

  const classTypes = [
    'AC First Class',
    'AC 2 Tier',
    'AC 3 Tier',
    'AC Chair Car',
    'Sleeper Class',
    'Second Sitting',
    'General',
  ];

  const amenities = [
    'WiFi',
    'Food Service',
    'Bedding',
    'Charging Points',
    'Reading Light',
    'Air Conditioning',
    'Luggage Rack',
    'Water Bottle',
  ];

  const handleFilterChange = (field: keyof SearchFilters, value: any) => {
    setFilters(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleSearch = () => {
    onSearch(filters);
  };

  const handleClear = () => {
    setFilters({
      fromStation: '',
      toStation: '',
      journeyDate: '',
      returnDate: '',
      trainType: [],
      classType: [],
      priceRange: [0, 5000],
      departureTime: '',
      arrivalTime: '',
      amenities: [],
      flexibleDates: false,
    });
  };

  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Search sx={{ mr: 1, color: 'primary.main' }} />
          <Typography variant="h6" component="h2">
            Advanced Train Search
          </Typography>
          <Button
            startIcon={<FilterList />}
            onClick={() => setShowAdvanced(!showAdvanced)}
            sx={{ ml: 'auto' }}
          >
            {showAdvanced ? 'Hide Filters' : 'Show Filters'}
          </Button>
        </Box>

        <Grid container spacing={2}>
          {/* Basic Search */}
          <Grid item xs={12} md={3}>
            <Autocomplete
              options={stations}
              value={filters.fromStation}
              onChange={(_, value) => handleFilterChange('fromStation', value || '')}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="From Station"
                  placeholder="Select departure station"
                />
              )}
            />
          </Grid>

          <Grid item xs={12} md={3}>
            <Autocomplete
              options={stations}
              value={filters.toStation}
              onChange={(_, value) => handleFilterChange('toStation', value || '')}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="To Station"
                  placeholder="Select destination station"
                />
              )}
            />
          </Grid>

          <Grid item xs={12} md={2}>
            <TextField
              type="date"
              label="Journey Date"
              value={filters.journeyDate}
              onChange={(e) => handleFilterChange('journeyDate', e.target.value)}
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
          </Grid>

          <Grid item xs={12} md={2}>
            <TextField
              type="date"
              label="Return Date"
              value={filters.returnDate}
              onChange={(e) => handleFilterChange('returnDate', e.target.value)}
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
          </Grid>

          <Grid item xs={12} md={2}>
            <Button
              variant="contained"
              startIcon={<Search />}
              onClick={handleSearch}
              fullWidth
              sx={{ height: '56px' }}
            >
              Search Trains
            </Button>
          </Grid>
        </Grid>

        {/* Advanced Filters */}
        {showAdvanced && (
          <Paper sx={{ mt: 3, p: 2, bgcolor: 'grey.50' }}>
            <Typography variant="h6" gutterBottom>
              Advanced Filters
            </Typography>

            <Grid container spacing={2}>
              {/* Train Type */}
              <Grid item xs={12} md={4}>
                <FormControl fullWidth>
                  <InputLabel>Train Type</InputLabel>
                  <Select
                    multiple
                    value={filters.trainType}
                    onChange={(e) => handleFilterChange('trainType', e.target.value)}
                    renderValue={(selected) => (
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                        {(selected as string[]).map((value) => (
                          <Chip key={value} label={value} size="small" />
                        ))}
                      </Box>
                    )}
                  >
                    {trainTypes.map((type) => (
                      <MenuItem key={type} value={type}>
                        {type}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              {/* Class Type */}
              <Grid item xs={12} md={4}>
                <FormControl fullWidth>
                  <InputLabel>Class Type</InputLabel>
                  <Select
                    multiple
                    value={filters.classType}
                    onChange={(e) => handleFilterChange('classType', e.target.value)}
                    renderValue={(selected) => (
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                        {(selected as string[]).map((value) => (
                          <Chip key={value} label={value} size="small" />
                        ))}
                      </Box>
                    )}
                  >
                    {classTypes.map((type) => (
                      <MenuItem key={type} value={type}>
                        {type}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              {/* Amenities */}
              <Grid item xs={12} md={4}>
                <FormControl fullWidth>
                  <InputLabel>Amenities</InputLabel>
                  <Select
                    multiple
                    value={filters.amenities}
                    onChange={(e) => handleFilterChange('amenities', e.target.value)}
                    renderValue={(selected) => (
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                        {(selected as string[]).map((value) => (
                          <Chip key={value} label={value} size="small" />
                        ))}
                      </Box>
                    )}
                  >
                    {amenities.map((amenity) => (
                      <MenuItem key={amenity} value={amenity}>
                        {amenity}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              {/* Price Range */}
              <Grid item xs={12} md={6}>
                <Typography gutterBottom>Price Range: ₹{filters.priceRange[0]} - ₹{filters.priceRange[1]}</Typography>
                <Slider
                  value={filters.priceRange}
                  onChange={(_, value) => handleFilterChange('priceRange', value)}
                  valueLabelDisplay="auto"
                  min={0}
                  max={10000}
                  step={100}
                />
              </Grid>

              {/* Time Filters */}
              <Grid item xs={12} md={3}>
                <TextField
                  type="time"
                  label="Departure Time"
                  value={filters.departureTime}
                  onChange={(e) => handleFilterChange('departureTime', e.target.value)}
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                />
              </Grid>

              <Grid item xs={12} md={3}>
                <TextField
                  type="time"
                  label="Arrival Time"
                  value={filters.arrivalTime}
                  onChange={(e) => handleFilterChange('arrivalTime', e.target.value)}
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                />
              </Grid>

              {/* Flexible Dates */}
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={filters.flexibleDates}
                      onChange={(e) => handleFilterChange('flexibleDates', e.target.checked)}
                    />
                  }
                  label="Flexible Dates (±3 days)"
                />
              </Grid>
            </Grid>

            {/* Action Buttons */}
            <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
              <Button
                variant="contained"
                startIcon={<Search />}
                onClick={handleSearch}
              >
                Apply Filters
              </Button>
              <Button
                variant="outlined"
                startIcon={<Clear />}
                onClick={handleClear}
              >
                Clear All
              </Button>
            </Box>
          </Paper>
        )}
      </CardContent>
    </Card>
  );
};

export default AdvancedSearch;
