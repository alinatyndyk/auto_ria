import axios from "axios";

import {baseURL} from "../constants";

const axiosService = axios.create({baseURL});

// axiosService.interceptors.request.use((config) => {
//     const access_token = authService.getAccessToken();
//     if (access_token) {
//         config.headers.access_token = `${access_token}`
//         return config
//     } else if (!access_token)
//         return config
// })

export {
    axiosService
}