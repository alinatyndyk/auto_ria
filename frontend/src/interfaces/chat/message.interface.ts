import {Pageable, Sort} from "../pagable.interface";

export interface IMessage {
    id: number,
    content: string,
    senderId: number,
    receiverId: number,
    chatId: number,
    isEdited: boolean | null,
    updatedAt: number[],
    createdAt: number[]
}

export interface IMessagePageResponse {
    content: IMessage[];
    pageable: Pageable;
    last: boolean;
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    sort: Sort;
    first: boolean;
    numberOfElements: number;
    empty: boolean;
}

export interface IChatsPageResponse {
    content: IChatResponse[];
    pageable: Pageable;
    last: boolean;
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    sort: Sort;
    first: boolean;
    numberOfElements: number;
    empty: boolean;
}

export interface IChatResponse {
    id: number;
    sellerId: number;
    customerId: number;
    sellerSessionId: string | null;
    customerSessionId: string | null;
    roomKey: string;
    notSeenCustomer: number,
    notSeenSeller: number

    createdAt: number[];
    updatedAt: number[];
}

export interface IGetChatMessagesRequest {
    page: number,
    sellerId: string,
    customerId: string
}

export interface IGetChatMessagesRequest {
    page: number,
    sellerId: string,
    customerId: string
}

export interface IGetChatMessagesOutlet {
    senderId: string,
    senderRole: string
}