import axios, {AxiosError} from "axios";

import {baseURL, geoURL} from "../constants";
import {authService} from "./auth.service";

const axiosService = axios.create({baseURL});
const axiosGeoService = axios.create({baseURL: geoURL});

axiosService.interceptors.request.use((config) => {
    const access_token = authService.getAccessToken();
    if (access_token) {
        config.headers.Authorization = `Bearer ${access_token}`;
    }
    return config;
});

let isRefreshing = false;
axiosService.interceptors.response.use((response) => {
        return response
    }, async (error: AxiosError) => {
    console.log(error, "AXIOS ERROR");
    // const refresh_token = authService.getRefreshToken();
    if(error.response?.status == 423) {
        console.log("423 ERROR");
    }
        // if (refresh_token && !isRefreshing) { //todo 401 only for refresh!!! //error.response?.status === 401 &&
        //     console.log("HELLO1");
        //     isRefreshing = true;
        //     console.log("is refresh");
        //     try {
        //         const refresh: IRefreshRequest = {refreshToken: refresh_token}
        //         const {data} = await authService.refresh(refresh);
        //         authService.setTokens(data);
        //     } catch (e) {
        //         authService.deleteTokens();
        //         // return history.replace('/account?ExpSession=true');
        //     }
        //     isRefreshing = false;
        //     console.log("refreshed");
        //     return axiosService(error.config);
        // } else if (!refresh_token) { //error.response?.status === 401 &&
        //     throw error
        // }
        // return Promise.reject(error);
    }
)

export {
    axiosService,
    axiosGeoService
}