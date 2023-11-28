import {Pageable, Sort} from "../pagable.interface";

export interface ICustomerInput {
    name: string,
    lastName: string,
    city: string,
    region: string,
    email: string,
    password: string,
    avatar: File | null
}

export interface ICustomerResponse {
    id: number;
    name: string;
    lastName: string;
    city: string;
    region: string;
    avatar: string;
    role: string;
    lastOnline: number[];
    createdAt: number[];
}

export interface ICustomerPageResponse {
    content: ICustomerResponse[];
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