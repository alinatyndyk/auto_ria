export interface IAuthResponse {
    accessToken: string,
    refreshToken: string,
}

export interface IAuthRequest {
    email: string,
    password: string,
}