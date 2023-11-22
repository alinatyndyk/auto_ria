import axios from "axios";

import {baseURL} from "../constants";
import {authService} from "./auth.service";
import {IRefreshRequest} from "../interfaces";

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
        if (error.response?.status === 401 && refresh_token && !isRefreshing) { //todo 401 only for refresh!!!
            isRefreshing = true;
            console.log("is refresh");
            try {

                const refresh: IRefreshRequest = {refreshToken: refresh_token}

                const {data} = await authService.refresh(refresh);
                authService.setTokens(data);
            } catch (e) {
                authService.deleteTokens();
                // return history.replace('/account?ExpSession=true');
            }
            isRefreshing = false;
            console.log("refreshed");
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