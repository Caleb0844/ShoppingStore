import { request, setAuthToken } from './httpClient';
import { LoginRequest, TokenResponse } from './models';

export const authApi = {
  async login(credentials: LoginRequest): Promise<TokenResponse> {
    const response = await request<TokenResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
    await setAuthToken(response.token);
    return response;
  },
};
