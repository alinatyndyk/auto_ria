import {Pageable, Sort} from "../pagable.interface";

export interface IAdminInput {
    name: string,
    lastName: string,
    email: string,
    password: string,
    avatar: File | null
}

export interface RegisterAdminPayload {
    adminInput: IAdminInput;
    code: string;
}

export interface IAdminResponse {
    id: number,
    name: string,
    lastName: string,
    email: string,
    avatar: File | null
}

export interface IAdminPageResponse {
    content: IAdminResponse[];
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