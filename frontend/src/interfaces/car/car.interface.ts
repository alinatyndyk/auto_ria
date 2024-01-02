import {Pageable, Sort} from "../pagable.interface";

export interface ICreateCar {
    brand: string;
    model: string;
    powerH: number;
    city: string;
    region: string;
    price: string;
    description: string;
    currency: string; //e
    pictures: File[];
}

export interface ICreateInputCar {
    brand: string;
    model: string;
    powerH: number;
    city: string;
    region: string;
    price: string;
    description: string;
    currency: string; //e
    pictures: File[];
}

export interface IUpdateInputCar {
    city: string;
    region: string;
    price: string;
    description: string;
    currency: string;
}

export interface IUpdateCarRequest {
    id: number,
    car: IUpdateInputCar
}

export interface ICar {
    id: number;
    brand: string;
    model: string;
    powerH: number;
    city: string;
    region: string;
    price: string;
    currency: string;
    photo: string[];
    description: string;
    seller: {
        id: number;
        name: string;
        lastName: string | null;
        city: string;
        region: string;
        role: string;
        number: string;
        avatar: string | null;
        createdAt: number[];
    };
    priceUAH: number;
    priceEUR: number;
    priceUSD: number;
}

export interface ICarResponse {
    content: ICar[];
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