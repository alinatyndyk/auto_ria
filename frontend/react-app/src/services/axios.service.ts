import axios, {AxiosError, AxiosRequestConfig} from "axios";

import {baseURL, geoURL, urls} from "../constants";
import {authService} from "./auth.service";

const axiosService = axios.create({baseURL});
const axiosGeoService = axios.create({baseURL: geoURL});

axiosService.interceptors.request.use(async (config) => {

    const access_token = authService.getAccessToken();

    if (config.url != urls.auth.refresh() && access_token) {
        console.log("auth/refresh not equal")
        config.headers.Authorization = `Bearer ${access_token}`;
    }
    return config;
});

let isRefreshing = false;
axiosService.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        const refresh_token = authService.getRefreshToken();

        if (error.response?.status === 423 && refresh_token && !isRefreshing) {
            isRefreshing = true;

            try {
                const refresh = authService.getRefreshToken();
                if (!refresh) {
                    throw new Error("refresh_token is required");
                }
                const { data } = await authService.refresh({ refreshToken: refresh });
                authService.setTokens(data);
                localStorage.setItem('isAuth', JSON.stringify(true));

                // Resend the original request with the new token
                const originalRequest = error.config;
                originalRequest.headers.Authorization = `Bearer ${data.access_token}`;
                return axiosService(originalRequest);
            } catch (e) {
                authService.deleteTokens();
                // return history.replace('/account?ExpSession=true');
            }
            isRefreshing = false;
        } else if (!refresh_token) {
            throw error;
        }
        return Promise.reject(error);
    }
);

export {
    axiosService,
    axiosGeoService
}