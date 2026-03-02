import api from './api';

export interface WorkplaceRequest {
  name: string;
  latitude: number;
  longitude: number;
  radius: number;
  address?: string;
}

export interface WorkplaceResponse {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
  radius: number;
  address?: string;
}

export const workplaceApi = {
  save: async (data: WorkplaceRequest): Promise<WorkplaceResponse> => {
    const response = await api.post('/workplace', data);
    return response.data;
  },

  get: async (): Promise<WorkplaceResponse> => {
    const response = await api.get('/workplace');
    return response.data;
  },
};
