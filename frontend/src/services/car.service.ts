import { IRes } from "../types/axiosRes.type";
import { CarsResponse, ICar, ICarResponse, ICreateCar, IUpdateInputCar } from "../interfaces";
import { axiosService } from "./axios.service";
import { urls } from "../constants";

const carService = {
    getAll: (page: number): IRes<ICarResponse> => axiosService.get(urls.cars.all(page)),
    getById: (id: number): IRes<CarsResponse> => axiosService.get(urls.cars.byId(id)),
    deleteById: (id: number): IRes<String> => axiosService.delete(urls.cars.deleteById(id)),
    banById: (id: number): IRes<String> => axiosService.post(urls.cars.banById(id)),
    unbanById: (id: number): IRes<String> => axiosService.post(urls.cars.unbanById(id)),
    getAllBrands: (): IRes<string[]> => axiosService.get(urls.cars.allBrands()),
    getAllModelsByBrand: (brand: string): IRes<string[]> => axiosService.get(urls.cars.allModelsByBrand(brand)),
    create: (car: ICreateCar): IRes<ICar> => axiosService.post(urls.cars.cars, car, {
        headers: {
            "Content-Type": "multipart/form-data",
        }
    }),
    updateById: (id: number, car: Partial<IUpdateInputCar>): IRes<ICar> => axiosService.patch(urls.cars.byId(id), car),
    getBySeller: (id: number, page: number): IRes<ICarResponse> => axiosService.get(urls.cars.bySeller(id, page)),
}

export {
    carService
}