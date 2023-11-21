import axios from "axios";

import {baseURL} from "../constants";
import {authService} from "./auth.service";

const axiosService = axios.create({baseURL});

axiosService.interceptors.request.use((config) => {
    const access_token = authService.getAccessToken();
    if (access_token) {
        config.headers.Authorization = `Bearer ${access_token}`;
    }
    return config;
});

let isRefreshing = false;
axiosService.interceptors.response.use((config) => {
        return config
    }, async (error) => {
        const refresh_token = authService.getRefreshToken();
        if (error.response?.status === 403) {
            throw error
        } else if (error.response?.status === 401 && refresh_token && !isRefreshing) {
            isRefreshing = true;
            try {
                const first = refresh_token.split(' ')[0];
                const {data} = await authService.refresh(refresh_token);
                authService.setTokens(data);
            } catch (e) {
                authService.deleteTokens();
                // return history.replace('/account?ExpSession=true');
            }
            isRefreshing = false;
            return axiosService(error.config);
        } else if (error.response?.status === 401 && !refresh_token) {
            throw error
        }
        return Promise.reject(error);
    }
)

export {
    axiosService
}