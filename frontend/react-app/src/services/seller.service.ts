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

    getChatMessages: (page: number): IRes<any> => axiosService.post(urls.chats.getChatMessages(page), {
        sellerId: 4,
        customerId: 3
    }, {
        headers: {
            "Content-Type": "multipart/form-data",
            Authorization: "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYWlzaWNyYWlzaUBnbWFpbC5jb20iLCJpYXQiOjE3MDEwMTQ2ODUsImlzcyI6IlNFTExFUiIsImV4cCI6MTcwMTAxODI4NX0.Oc3HQy2SKRm2FH5rgMrm-rBTBy2Pqc5Zhhh5atD7F7k"
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