import { urls } from "../constants";
import { ERole } from "../constants/role.enum";
import {
    IActivationCode,
    IAuthRequest,
    IAuthResponse,
    ICar,
    ICarResponse,
    IChangePassword,
    ICreateCar, IForgotPassword,
    IGenerateCode,
    INewPassword,
    IRefreshRequest,
} from "../interfaces";
import { IChatsResponse } from "../interfaces/chat.interface";
import { ISellerInput } from "../interfaces/user/seller.interface";
import { IRes } from "../types/axiosRes.type";
import { axiosService } from "./axios.service";

const _accessTokenKey: string = 'access_token'
const _refreshTokenKey: string = 'refresh_token'
const _isAuth: string = 'isAuth'

const chatService = {
    getChatsByUser: (page: number): IRes<IChatsResponse> => axiosService.get(urls.chats.getChatsByUser(page)),
}

export {
    chatService
};

