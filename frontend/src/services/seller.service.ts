import { urls } from "../constants";
import { IGetChatMessagesRequest } from "../interfaces/chat/message.interface";
import { IGeoCitiesResponse, IGeoRegionsResponse } from "../interfaces/geo.interface";
import { ISellerPageResponse, IUserResponse, IUserUpdateRequest } from "../interfaces/user/seller.interface";
import { IRes } from "../types/axiosRes.type";
import { axiosGeoService, axiosService } from "./axios.service";

const sellerService = {
    getAll: (page: number): IRes<ISellerPageResponse> => axiosService.get(urls.sellers.all(page)),
    getById: (id: number): IRes<IUserResponse> => axiosService.get(urls.users.getById(id)),
    deleteById: (id: number): IRes<String> => axiosService.delete(urls.users.deleteById(id)),
    updateById: (id: number, body: Partial<IUserUpdateRequest>): IRes<IUserResponse> => axiosService.patch(urls.users.updateById(id), body),

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
}

export {
    sellerService
};

