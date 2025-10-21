import React, { createContext, useContext, useState, ReactNode } from 'react';

interface BookingContextType {
  searchParams: SearchParams | null;
  selectedTrain: Train | null;
  selectedSeats: Seat[];
  bookingData: BookingData | null;
  setSearchParams: (params: SearchParams) => void;
  setSelectedTrain: (train: Train) => void;
  setSelectedSeats: (seats: Seat[]) => void;
  setBookingData: (data: BookingData) => void;
  clearBooking: () => void;
}

interface SearchParams {
  sourceStationCode: string;
  destinationStationCode: string;
  journeyDate: string;
  numberOfPassengers: number;
  preferredClass?: string;
}

interface Train {
  id: number;
  trainNumber: string;
  trainName: string;
  sourceStationCode: string;
  sourceStationName: string;
  destinationStationCode: string;
  destinationStationName: string;
  departureTime: string;
  arrivalTime: string;
  journeyDuration: string;
  totalDistance: number;
  trainType: string;
  status: string;
  isRunning: boolean;
  availableSeats: number;
  startingFare: number;
  isTatkalAvailable: boolean;
  isPremiumTatkalAvailable: boolean;
}

interface Seat {
  id: number;
  seatNumber: string;
  seatType: string;
  berthType: string;
  isAvailable: boolean;
  isLadiesQuota: boolean;
  isSeniorCitizenQuota: boolean;
  isHandicappedFriendly: boolean;
  fare: number;
}

interface BookingData {
  trainId: number;
  passengerId: number;
  seatId: number;
  coachId: number;
  journeyDate: string;
  totalFare: number;
  baseFare: number;
  quotaType: string;
  isTatkal: boolean;
}

const BookingContext = createContext<BookingContextType | undefined>(undefined);

export const useBooking = () => {
  const context = useContext(BookingContext);
  if (context === undefined) {
    throw new Error('useBooking must be used within a BookingProvider');
  }
  return context;
};

interface BookingProviderProps {
  children: ReactNode;
}

export const BookingProvider: React.FC<BookingProviderProps> = ({ children }) => {
  const [searchParams, setSearchParams] = useState<SearchParams | null>(null);
  const [selectedTrain, setSelectedTrain] = useState<Train | null>(null);
  const [selectedSeats, setSelectedSeats] = useState<Seat[]>([]);
  const [bookingData, setBookingData] = useState<BookingData | null>(null);

  const clearBooking = () => {
    setSearchParams(null);
    setSelectedTrain(null);
    setSelectedSeats([]);
    setBookingData(null);
  };

  const value = {
    searchParams,
    selectedTrain,
    selectedSeats,
    bookingData,
    setSearchParams,
    setSelectedTrain,
    setSelectedSeats,
    setBookingData,
    clearBooking,
  };

  return <BookingContext.Provider value={value}>{children}</BookingContext.Provider>;
};
