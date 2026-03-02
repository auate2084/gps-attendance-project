import api from './api';

export interface CheckinRequest {
  latitude: number;
  longitude: number;
}

export interface CheckoutRequest {
  latitude: number;
  longitude: number;
}

export interface AttendanceResponse {
  id: number;
  checkinTime: string;
  checkinLatitude: number;
  checkinLongitude: number;
  checkoutTime?: string;
  checkoutLatitude?: number;
  checkoutLongitude?: number;
  status: string;
  workplaceName: string;
}

export interface AttendanceStatusResponse {
  isCheckedIn: boolean;
  checkinTime?: string;
  workplaceName?: string;
}

export const attendanceApi = {
  checkin: async (data: CheckinRequest): Promise<AttendanceResponse> => {
    const response = await api.post('/attendance/checkin', data);
    return response.data;
  },

  checkout: async (data: CheckoutRequest): Promise<AttendanceResponse> => {
    const response = await api.post('/attendance/checkout', data);
    return response.data;
  },

  getHistory: async (): Promise<AttendanceResponse[]> => {
    const response = await api.get('/attendance/history');
    return response.data;
  },

  getTodayHistory: async (): Promise<AttendanceResponse[]> => {
    const response = await api.get('/attendance/today');
    return response.data;
  },

  getStatus: async (): Promise<AttendanceStatusResponse> => {
    const response = await api.get('/attendance/status');
    return response.data;
  },
};
