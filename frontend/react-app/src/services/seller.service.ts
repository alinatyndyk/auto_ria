import {IRes} from "../types/axiosRes.type";
import {ICar, ICarResponse, ICreateCar} from "../interfaces";
import {axiosService} from "./axios.service";
import {urls} from "../constants";
import {ISellerPageResponse, ISellerResponse} from "../interfaces/user/seller.interface";
import {ICustomerResponse} from "../interfaces/user/customer.interface";
import {IAdminResponse} from "../interfaces/user/admin.interface";
import {IManagerResponse} from "../interfaces/user/manager.interface";
import {authService} from "./auth.service";

const sellerService = {
    getAll: (page: number): IRes<ISellerPageResponse> => axiosService.get(urls.sellers.all(page)),
    getById: (id: number): IRes<ISellerResponse> => axiosService.put(urls.users.getById(id)),
    getByToken: (): IRes<any> => axiosService.get(urls.users.getByToken(), {
        headers: {
            Authorization: `Bearer ${authService.getAccessToken()}`
        }
    }),
    isSellerResponse(obj: any): obj is ISellerResponse {
        return (
            typeof obj.id === 'number' &&
            typeof obj.name === 'string' &&
            typeof obj.lastName === 'string' &&
            typeof obj.city === 'string' &&
            typeof obj.region === 'string' &&
            typeof obj.number === 'string' &&
            (obj.avatar === null || typeof obj.avatar === 'string') &&
            Array.isArray(obj.createdAt) // Check if createdAt is an array
        );
    },
    isManagerResponse(obj: any): obj is IManagerResponse {
        return (
            typeof obj.id === 'number' &&
            typeof obj.name === 'string' &&
            typeof obj.lastName === 'string' &&
            (obj.avatar === null || typeof obj.avatar === 'string') &&
            Array.isArray(obj.createdAt) &&
            obj.role === 'manager'
        );
    },
    isCustomerResponse(obj: any): obj is ICustomerResponse {
        return (
            typeof obj.id === 'number' &&
            typeof obj.name === 'string' &&
            typeof obj.lastName === 'string' &&
            typeof obj.city === 'string' &&
            typeof obj.region === 'string' &&
            (obj.avatar === null || typeof obj.avatar === 'string') &&
            Array.isArray(obj.createdAt) &&
            obj.role === 'customer'
        );
    },
    isAdminResponse(obj: any): obj is IAdminResponse {
        return (
            typeof obj.id === 'number' &&
            typeof obj.name === 'string' &&
            typeof obj.lastName === 'string' &&
            typeof obj.email === 'string' &&
            (obj.avatar === null || typeof obj.avatar === 'string') &&
            Array.isArray(obj.createdAt) &&
            obj.role === 'admin'  //todo move to common
        );
    }
}

export {
    sellerService
}