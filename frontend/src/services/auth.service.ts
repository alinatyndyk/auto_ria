import { urls } from "../constants";
import { ERole } from "../constants/role.enum";
import {
    IActivationCode,
    IAuthRequest,
    IAuthResponse,
    ICar,
    ICarResponse,
    IChangePassword,
    ICreateCar, IForgotPassword,
    IGenerateCode,
    INewPassword,
    IRefreshRequest,
} from "../interfaces";
import { ISellerInput } from "../interfaces/user/seller.interface";
import { IRes } from "../types/axiosRes.type";
import { axiosService } from "./axios.service";

const _accessTokenKey: string = 'access_token'
const _refreshTokenKey: string = 'refresh_token'
const _isAuth: string = 'isAuth'

const authService = {
    getAll: (page: number): IRes<ICarResponse> => axiosService.get(urls.cars.all(page)),
    create: (car: ICreateCar): IRes<ICar> => axiosService.post(urls.cars.cars, car, {
        headers: {
            "Content-Type": "multipart/form-data",
        }
    }),
    login: (info: IAuthRequest) => axiosService.post(urls.auth.login(), info),
    signOut: () => axiosService.post(urls.auth.signOut(), {}),
    refresh: (refresh: IRefreshRequest) => axiosService.post(urls.auth.refresh(), {
        refreshToken: refresh.refreshToken
    }),
    registerSeller: (info: ISellerInput) => axiosService.post(urls.auth.registerSeller(), info, {
        headers: {
            "Content-Type": "multipart/form-data",
        }
    }),
    registerUserAuth: (info: ISellerInput) => axiosService.post(urls.auth.registerUserAuth(), info, {
        headers: {
            "Content-Type": "multipart/form-data",
        }
    }),

    activateSeller: (codeInterface: IActivationCode) => axiosService.post(urls.auth.activateSeller(), {
        code: codeInterface.code
    }, {
        headers: {
            "Content-Type": "multipart/form-data"
        }
    }),

    generateManager: (emailInterface: IGenerateCode) => axiosService.post(urls.auth.generateManager(), {
        email: emailInterface.email
    }, {
        headers: {
            "Content-Type": "multipart/form-data"
        }
    }),

    toManager: (emailInterface: IGenerateCode) => axiosService.post(urls.auth.toManager(), {
        email: emailInterface.email,
        role: ERole.MANAGER
    }, {
        headers: {
            "Content-Type": "multipart/form-data"
        }
    }),

    generateAdmin: (emailInterface: IGenerateCode) => axiosService.post(urls.auth.generateAdmin(), {
        email: emailInterface.email
    }, {
        headers: {
            "Content-Type": "multipart/form-data"
        }
    }),

    changePassword: (passInterface: INewPassword) => axiosService.post(urls.auth.changePassword(), {
        newPassword: passInterface.newPassword
    }, {
        headers: {
            "Content-Type": "multipart/form-data"
        }
    }),

    forgotPassword: (forgotInterface: IForgotPassword) => axiosService.post(urls.auth.forgotPassword(), {
        email: forgotInterface.email
    }, {
        headers: {
            "Content-Type": "multipart/form-data",
        }
    }),

    resetPassword: (info: IChangePassword) => axiosService.post(urls.auth.resetPassword(), {
        newPassword: info.newPassword
    }, {
        headers: {
            "Content-Type": "multipart/form-data",
            "Register-key": info.code
        }
    }),

    setTokens: ({ accessToken, refreshToken }: IAuthResponse) => {
        localStorage.setItem(_accessTokenKey, accessToken);
        localStorage.setItem(_refreshTokenKey, refreshToken);
    },
    deleteTokens: () => {
        localStorage.removeItem(_accessTokenKey);
        localStorage.removeItem(_refreshTokenKey);
        localStorage.removeItem(_isAuth);
    },

    getAccessToken: () => {
        return localStorage.getItem(_accessTokenKey);
    },

    getRefreshToken: () => {
        return localStorage.getItem(_refreshTokenKey);
    },
}

export {
    authService
};

