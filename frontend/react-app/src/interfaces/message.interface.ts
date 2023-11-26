import {Pageable, Sort} from "./pagable.interface";
import {ISellerResponse} from "./user/seller.interface";

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