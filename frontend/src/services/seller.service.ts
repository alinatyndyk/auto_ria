import { IRes } from "../types/axiosRes.type";
import { axiosGeoService, axiosService } from "./axios.service";
import { urls } from "../constants";
import { ISellerPageResponse, ISellerResponse, IUserResponse, IUserUpdateRequest } from "../interfaces/user/seller.interface";
import { ICustomerResponse } from "../interfaces/user/customer.interface";
import { IAdminResponse } from "../interfaces/user/admin.interface";
import { IManagerResponse } from "../interfaces/user/manager.interface";
import { authService } from "./auth.service";
import { IGeoCitiesResponse, IGeoRegionsResponse } from "../interfaces/geo.interface";
import { ERole } from "../constants/role.enum";
import { IGetChatMessagesRequest } from "../interfaces/chat/message.interface";

const sellerService = {
    getAll: (page: number): IRes<ISellerPageResponse> => axiosService.get(urls.sellers.all(page)),
    getById: (id: number): IRes<ISellerResponse> => axiosService.get(urls.users.getById(id)),
    deleteById: (id: number): IRes<String> => axiosService.delete(urls.users.deleteById(id)),
    getCustomerById: (id: number): IRes<ICustomerResponse> => axiosService.get(urls.customers.getById(id)),
    getSellerById: (id: number): IRes<ISellerResponse> => axiosService.get(urls.sellers.getById(id)),
    updateById: (id: number, body: Partial<IUserUpdateRequest>): IRes<IUserResponse> => axiosService.patch(urls.users.updateById(id), {
        body
    }, {
        headers: {
                "Content-Type": "multipart/form-data"
            }
    }),
    getByToken: (token: string): IRes<any> => axiosService.get(urls.users.getByToken(token)),

    getChatMessages: ({ sellerId, customerId, page }: IGetChatMessagesRequest):
        IRes<any> => axiosService.post(urls.chats.getChatMessages(page), {
            sellerId,
            customerId
        }, {
            headers: {
                "Content-Type": "multipart/form-data"
            }
        }),

    getChatsByUserToken: (page: number): IRes<any> => axiosService.get(urls.chats.getChatsByUserToken(page)),
    getRegionsByPrefix: (prefix: string): IRes<IGeoRegionsResponse> => axiosGeoService.get(urls.geo.getRegionsByPrefix(prefix)),
    getRegionsPlaces: (regionId: string): IRes<IGeoCitiesResponse> => axiosGeoService.get(urls.geo.getRegionsPlaces(regionId)),

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
            obj.role === ERole.MANAGER
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
            obj.role === ERole.CUSTOMER
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
            obj.role === ERole.ADMIN
        );
    }
}

export {
    sellerService
}