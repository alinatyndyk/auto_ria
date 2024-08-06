import { urls } from "../constants";
import { CarsResponse, ICar, ICarResponse, ICreateCar, IMiddleCarValues, IUpdateInputCar } from "../interfaces";
import { IRes } from "../types/axiosRes.type";
import { axiosService } from "./axios.service";

const carService = {
    getAll: (page: number): IRes<ICarResponse> => axiosService.get(urls.cars.all(page)),
    getById: (id: number): IRes<CarsResponse> => axiosService.get(urls.cars.byId(id)),
    getMiddleById: (id: number): IRes<IMiddleCarValues> => axiosService.get(urls.cars.byMiddleId(id)),
    deleteById: (id: number): IRes<String> => axiosService.delete(urls.cars.deleteById(id)),
    banById: (id: number): IRes<String> => axiosService.post(urls.cars.banById(id)),
    unbanById: (id: number): IRes<String> => axiosService.post(urls.cars.unbanById(id)),
    deletePhotos: (carId: number, photos: string[]): IRes<String> => axiosService.post(urls.cars.deletePhotos(carId), { photos }),
    addPhotos: (carId: number, photos: FormData): Promise<IRes<String[]>> => axiosService.post(urls.cars.addPhotos(carId), photos, {
        headers: {
            "Content-Type": "multipart/form-data",
        }
    }),
    getAllBrands: (): IRes<string[]> => axiosService.get(urls.cars.allBrands()),
    getAllModelsByBrand: (brand: string): IRes<string[]> => axiosService.get(urls.cars.allModelsByBrand(brand)),
    create: (car: ICreateCar): IRes<CarsResponse> => axiosService.post(urls.cars.cars, car, {
        headers: {
            "Content-Type": "multipart/form-data",
        }
    }),
    updateById: (id: number, car: Partial<IUpdateInputCar>): IRes<CarsResponse> => axiosService.patch(urls.cars.byId(id), car),
    getBySeller: (id: number, page: number): IRes<ICarResponse> => axiosService.get(urls.cars.bySeller(id, page)),
}

export {
    carService
};
