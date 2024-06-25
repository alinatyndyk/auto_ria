import axios, { AxiosError } from "axios";

import { baseURL, geoURL, urls } from "../constants";
import { authService } from "./auth.service";

const axiosService = axios.create({ baseURL });
const axiosGeoService = axios.create({ baseURL: geoURL });

axiosService.interceptors.request.use(async (config) => {

    const access_token = authService.getAccessToken();

    if (config.url != urls.auth.refresh() && access_token) {
        config.headers.Authorization = `Bearer ${access_token}`;

    }
    return config;
});

let isRefreshing = false;
let failedQueue: { resolve: (token: string) => void; reject: (error: AxiosError) => void }[] = [];

const processQueue = (error: AxiosError | null, token: string | null = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token as string);
        }
    });

    failedQueue = [];
}

axiosService.interceptors.response.use(
    (response) => response,
    async (error) => {
        const refresh_token = authService.getRefreshToken();
        if (error.response?.status === 423 && refresh_token) {
            if (!isRefreshing) {
                isRefreshing = true;
                try {
                    const { data } = await authService.refresh({ refreshToken: refresh_token });
                    authService.setTokens(data);
                    processQueue(null, data.access_token);
                } catch (e) {
                    processQueue(e as AxiosError, null);
                    authService.deleteTokens();
                } finally {
                    isRefreshing = false;
                }
            }

            return new Promise((resolve, reject) => {
                failedQueue.push({ resolve, reject });
            }).then(token => {
                error.config.headers.Authorization = `Bearer ${token}`;
                return axiosService(error.config);
            }).catch(err => {
                return Promise.reject(err);
            });
        }
        return Promise.reject(error);
    }
);

export {
    axiosGeoService, axiosService
};
