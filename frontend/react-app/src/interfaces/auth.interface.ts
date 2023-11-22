export interface IAuthResponse {
    accessToken: string,
    refreshToken: string,
}

export interface IAuthRequest {
    email: string,
    password: string,
}

export interface IRefreshRequest {
    refreshToken: string
}

export interface IActivationCode {
    code: string
}

export interface IGenerateCode {
    email: string
}

export interface IChangePassword {
    newPassword: string,
    code: string
}

export interface INewPassword {
    newPassword: string
}

export interface IForgotPassword {
    email: string
}