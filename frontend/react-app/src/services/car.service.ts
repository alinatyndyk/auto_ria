import {IRes} from "../types/axiosRes.type";
import {ICar, ICarResponse, ICreateCar} from "../interfaces";
import {axiosService} from "./axios.service";
import {urls} from "../constants";

const carService = {
    getAll: (page: number): IRes<ICarResponse> => axiosService.get(urls.cars.all(page)),
    getById: (id: number): IRes<ICar> => axiosService.get(urls.cars.byId(id)),
    getAllBrands: (): IRes<string[]> => axiosService.get(urls.cars.allBrands()),
    getAllModelsByBrand: (brand: string): IRes<string[]> => axiosService.get(urls.cars.allModelsByBrand(brand)),
    create: (car: ICreateCar): IRes<ICar> => axiosService.post(urls.cars.cars, car, {
        headers: {
            "Content-Type": "multipart/form-data",
            // Authorization: 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGluYXR5bmR5azc3N0BnbWFpbC5jb20iLCJpYXQiOjE3MDA1Njc5NzEsImlzcyI6IkFETUlOIiwiZXhwIjoxNzAwNTcxNTcxfQ.Vzl_YoRw_pJZoVLYzuqI0jbciHpWoWk0PhdVdQjEErQ'
        }
    }),
    updateById: (id: number, car: ICar): IRes<ICar> => axiosService.put(urls.cars.byId(id), car),
    deleteById: (id: number): IRes<void> => axiosService.delete(urls.cars.byId(id)),
    getBySeller: (id: number, page: number): IRes<ICarResponse> => axiosService.get(urls.cars.bySeller(id, page)),
}

export {
    carService
}