import axios from "axios";

import { baseURL, geoURL, urls } from "../constants";
import { authService } from "./auth.service";
import { useAppDispatch } from "../hooks";
import { sellerActions } from "../redux/slices/seller.slice";
import { securityService } from "./security.service";

const axiosService = axios.create({ baseURL });
const axiosGeoService = axios.create({ baseURL: geoURL });

axiosService.interceptors.request.use(async (config) => {

    const access_token = authService.getAccessToken();

    if (config.url != urls.auth.refresh() && access_token) {
        config.headers.Authorization = `Bearer ${access_token}`;

    }
    console.log(JSON.stringify(config.headers));
    return config;
});

let isRefreshing = false;
axiosService.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        const refresh_token = authService.getRefreshToken();
        console.log("INTER REFRESH")
        console.log(error.response.status + "error st");
        if (error.response?.status === 423 && refresh_token && !isRefreshing) {
            isRefreshing = true;

            console.log("423 error");

            try {
                const refresh = authService.getRefreshToken();
                if (!refresh) {
                    throw new Error("refresh_token is required");
                }
                const { data } = await authService.refresh({ refreshToken: refresh });
                authService.setTokens(data);
                localStorage.setItem('isAuth', JSON.stringify(true));

                // Resend the original request with the new token

                return axiosService(error.config);
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