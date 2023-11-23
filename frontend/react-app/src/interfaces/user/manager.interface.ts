import {Pageable, Sort} from "../pagable.interface";

export interface IManagerInput {
    name: string,
    lastName: string,
    email: string,
    password: string,
    avatar: File | null
}

export interface RegisterManagerPayload {
    managerInput: IManagerInput;
    code: string;
}

export interface IManagerResponse {
    id: number,
    name: string,
    lastName: string,
    email: string,
    avatar: File | null
}

export interface IManagerPageResponse {
    content: IManagerResponse[];
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