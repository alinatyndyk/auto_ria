import { Pageable, Sort } from "../pagable.interface";

export interface ISellerInput {
    name: string,
    lastName: string,
    city: string,
    region: string,
    email: string,
    number: string,
    password: string,
    avatar: File
    code: string | null
}

export interface ISellerResponse {
    id: number;
    name: string;
    lastName: string;
    city: string;
    region: string;
    number: string;
    avatar: string;
    accountType: string;
    role: string,
    paymentSourcePresent: boolean,
    createdAt: number[];
}

export interface ISellerPageResponse {
    content: ISellerResponse[];
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


export interface IUserResponse {
    id: number;
    name: string;
    lastName: string;
    city: string;
    region: string;
    number: string;
    avatar?: string | null;
    accountType: 'BASIC' | 'PREMIUM';
    role: 'USER' | 'ADMIN' | 'MANAGER';
    isPaymentSourcePresent: boolean;
    lastOnline?: string | null;
    createdAt: string;
}

export interface IUserUpdateRequest {
    city: string;
    region: string;
    number: string;
    name: string;
    lastName: string;
}

export interface IUserUpdateRequestWithId {
    id: number;
    body: Partial<IUserUpdateRequest>;
}