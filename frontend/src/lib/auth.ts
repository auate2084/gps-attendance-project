import api from './api';

export interface RegisterRequest {
  loginId?: string;
  email: string;
  password: string;
  name: string;
  teamId?: number;
}

export interface LoginRequest {
  loginId: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: {
    id: number;
    loginId: string;
    email: string;
    name: string;
    teamId?: number | null;
  };
}

export const authApi = {
  register: async (data: RegisterRequest): Promise<void> => {
    await api.post('/users/register', {
      loginId: data.loginId ?? data.email,
      password: data.password,
      email: data.email,
      name: data.name,
      teamId: data.teamId ?? 1,
    });
  },

  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post('/users/login', data);
    return response.data;
  },

  refresh: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await api.post('/users/refresh', { refreshToken });
    return response.data;
  },
};
