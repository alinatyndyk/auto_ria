import {axiosService} from "./axios.service";
import {baseURL, urls} from "../constants/urls";
import {IRes} from "../interfaces/response.interface";
import {ICar} from "../interfaces/car.interface";

const carsService = {
    getAll: () => axiosService.get(urls.cars),
    // getAllPage: (page: {page: number}) => axiosService.get(urls.cars, page),
    getById: (_id: number) => axiosService.get(`${urls.cars}/${_id}`),
}

export {
    carsService
}