export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: string;
  username: string;
  fullName: string;
  email: string;
  roles: string[];
}

export interface LoginRequest {
  login: string;
  password: string;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface User {
  userId: string;
  username: string;
  fullName: string;
  email: string;
  roles: string[];
}