import api from './api';

export interface WorkplaceRequest {
  name: string;
  latitude: number;
  longitude: number;
  radius: number;
  address?: string;
}

export interface WorkplaceResponse {
  id: number | string;
  name: string;
  latitude: number;
  longitude: number;
  radius: number;
  address?: string;
}

interface TeamSummary {
  id: number;
  name: string;
}

const WORKPLACE_STORAGE_KEY = 'workplace';

export const workplaceApi = {
  save: async (data: WorkplaceRequest): Promise<WorkplaceResponse> => {
    const stored: WorkplaceResponse = {
      id: 'local',
      ...data,
    };
    if (typeof window !== 'undefined') {
      localStorage.setItem(WORKPLACE_STORAGE_KEY, JSON.stringify(stored));
    }

    const teamId =
      (typeof window !== 'undefined' && Number(localStorage.getItem('teamId'))) || 1;

    try {
      await api.post('/teams/work-policies', {
        teamId,
        name: data.name,
        latitude: data.latitude,
        longitude: data.longitude,
        checkinRadiusM: data.radius,
        checkoutRadiusM: data.radius,
        checkoutGraceMinutes: 10,
      });
    } catch {
      // Keep local config even if server-side policy creation fails due permissions.
    }

    return stored;
  },

  get: async (): Promise<WorkplaceResponse> => {
    if (typeof window === 'undefined') {
      throw new Error('Workplace is only available in browser runtime');
    }
    const raw = localStorage.getItem(WORKPLACE_STORAGE_KEY);
    if (!raw) {
      throw new Error('No workplace configured');
    }
    return JSON.parse(raw) as WorkplaceResponse;
  },

  listTeams: async (): Promise<TeamSummary[]> => {
    const response = await api.get<TeamSummary[]>('/teams');
    return response.data;
  },
};
