import {IRes} from "../types/axiosRes.type";
import {ICar, ICarResponse, ICreateCar} from "../interfaces";
import {axiosService} from "./axios.service";
import {urls} from "../constants";

const carService = {
    getAll: (page: number): IRes<ICarResponse> => axiosService.get(urls.cars.all(page)),
    create: (car: ICreateCar): IRes<ICar> => axiosService.post(urls.cars.cars, car, {
        headers: {
            "Content-Type": "multipart/form-data",
            Authorization: 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGluYXR5bmR5azc3N0BnbWFpbC5jb20iLCJpYXQiOjE3MDA1MjY0MjcsImlzcyI6IkFETUlOIiwiZXhwIjoxNzAwNTMwMDI3fQ.yfPgwDFJV-u8ewQc7LSFFT_OhzwBIIL_xq8FZrxGsY8'
        }
    }),
    updateById: (id: number, car: ICar): IRes<ICar> => axiosService.put(urls.cars.byId(id), car), //todo add headers token
    deleteById: (id: number): IRes<void> => axiosService.delete(urls.cars.byId(id)),
}

export {
    carService
}