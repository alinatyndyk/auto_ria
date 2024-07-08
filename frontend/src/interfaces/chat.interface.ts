import { Pageable, Sort } from "./pagable.interface";

export interface MessageClass {
    id: number;
    content: string;
    senderId: string;
    receiverId: string;
    chatId: number;
    isEdited: boolean;
    isSeen: boolean;
    updatedAt: any; // Assuming LocalDateTime is serialized as string in ISO format
    createdAt: any; // Assuming LocalDateTime is serialized as string in ISO format;
}


export interface IChatResponse {
    id: number;
    messages: MessageClass[];
    users: number[];
    sessions: string[];
    notSeenUser1: number;
    notSeenUser2: number;
    roomKey: string;
    createdAt: any; // Assuming LocalDateTime is serialized as string in ISO format
    updatedAt: any; // Assuming LocalDateTime is serialized as string in ISO format

    addMessage(message: MessageClass): void;
}

export interface IChatsResponse {
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

export interface IMsgsOfChatRequest {
    page: number,
    yourId: number,
    secondId: number
}

export interface IMsgsOfChatResponse {
    content: MessageClass[];
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