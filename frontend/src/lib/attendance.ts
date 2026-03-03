import api from './api';

export interface CheckinRequest {
  latitude: number;
  longitude: number;
  observedAt?: string;
}

export interface CheckoutRequest {
  latitude: number;
  longitude: number;
  observedAt?: string;
}

interface WorkSessionResponse {
  sessionId: number;
  userId: number;
  userName: string;
  status: string;
  checkInAt: string;
  checkOutAt?: string | null;
}

interface PageableResponse<T> {
  content: T[];
}

export interface AttendanceResponse {
  id: number;
  checkinTime: string;
  checkinLatitude?: number;
  checkinLongitude?: number;
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

export interface LocationUpdateResponse {
  state: string;
  message: string;
  sessionId?: number | null;
  distanceM?: number | null;
}

const mapSession = (session: WorkSessionResponse): AttendanceResponse => ({
  id: session.sessionId,
  checkinTime: session.checkInAt,
  checkoutTime: session.checkOutAt ?? undefined,
  status: session.status,
  workplaceName: session.userName,
});

export const attendanceApi = {
  checkin: async (data: CheckinRequest): Promise<LocationUpdateResponse> => {
    const response = await api.post('/attendance/me/location', data);
    return response.data;
  },

  checkout: async (data: CheckoutRequest): Promise<LocationUpdateResponse> => {
    const response = await api.post('/attendance/me/location', data);
    return response.data;
  },

  getHistory: async (): Promise<AttendanceResponse[]> => {
    const response = await api.get<PageableResponse<WorkSessionResponse>>('/attendance/me/sessions', {
      params: { page: 0, size: 200 },
    });
    return response.data.content.map(mapSession);
  },

  getTodayHistory: async (): Promise<AttendanceResponse[]> => {
    const today = new Date().toDateString();
    const all = await attendanceApi.getHistory();
    return all.filter((item) => new Date(item.checkinTime).toDateString() === today);
  },

  getStatus: async (): Promise<AttendanceStatusResponse> => {
    const response = await api.get<PageableResponse<WorkSessionResponse>>('/attendance/me/sessions', {
      params: { page: 0, size: 1 },
    });
    const latest = response.data.content[0];
    if (!latest) {
      return { isCheckedIn: false };
    }
    return {
      isCheckedIn: latest.status === 'CHECKED_IN',
      checkinTime: latest.checkInAt,
      workplaceName: latest.userName,
    };
  },
};
