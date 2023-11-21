import {IRes} from "../types/axiosRes.type";
import {IAuthRequest, IAuthResponse, ICar, ICarResponse, ICreateCar} from "../interfaces";
import {axiosService} from "./axios.service";
import {urls} from "../constants";

const _accessTokenKey: string = 'access_token'
const _refreshTokenKey: string = 'refresh_token'

const authService = {
    getAll: (page: number): IRes<ICarResponse> => axiosService.get(urls.cars.all(page)),
    create: (car: ICreateCar): IRes<ICar> => axiosService.post(urls.cars.cars, car, {
        headers: {
            "Content-Type": "multipart/form-data",
            Authorization: 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGluYXR5bmR5azc3N0BnbWFpbC5jb20iLCJpYXQiOjE3MDA1Njc5NzEsImlzcyI6IkFETUlOIiwiZXhwIjoxNzAwNTcxNTcxfQ.Vzl_YoRw_pJZoVLYzuqI0jbciHpWoWk0PhdVdQjEErQ'
        }
    }),
    login: (info: IAuthRequest) => axiosService.post(`${urls.auth.login}`, info),
    refresh: (refresh_token: string) => axiosService.post(`${urls.auth.refresh}`, refresh_token, {
        headers: {
            refresh_token: `${authService.getRefreshToken()}`
        }
    }),



    setTokens: ({accessToken, refreshToken}: IAuthResponse) => {
        localStorage.setItem(_accessTokenKey, accessToken)
        localStorage.setItem(_refreshTokenKey, refreshToken)
    },
    deleteTokens: () => {
        localStorage.removeItem(_accessTokenKey)
        localStorage.removeItem(_refreshTokenKey)
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
}