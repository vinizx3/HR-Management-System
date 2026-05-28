export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
}

export interface TokenPayload {
  sub: string;
  role: string;
  exp: number;
}
