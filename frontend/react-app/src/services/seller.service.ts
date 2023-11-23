import {IRes} from "../types/axiosRes.type";
import {ICar, ICarResponse, ICreateCar} from "../interfaces";
import {axiosService} from "./axios.service";
import {urls} from "../constants";
import {ISellerPageResponse, ISellerResponse} from "../interfaces/user/seller.interface";

const sellerService = {
    getAll: (page: number): IRes<ISellerPageResponse> => axiosService.get(urls.sellers.all(page)),
    getById: (id: number): IRes<ISellerResponse> => axiosService.put(urls.users.getById(id)),
}

export {
    sellerService
}