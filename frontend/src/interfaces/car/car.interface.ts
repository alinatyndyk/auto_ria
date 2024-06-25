import { Pageable, Sort } from "../pagable.interface";

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
    car: Partial<IUpdateInputCar>
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
    content: CarsResponse[];
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

export interface CarsResponse {
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
    isActivated: boolean,
    user: UserCarResponse;
    priceUAH: number;
    priceEUR: number;
    priceUSD: number;
    createdAt: string; // Assuming createdAt is a string representation of a date
}


export interface UserCarResponse {
    id: number;
    name: string;
    lastName: string;
    city: string;
    region: string;
    number: string;
    role: string;
    avatar: string;
    createdAt: string; // Assuming createdAt is a string representation of a date
}

export interface IMiddleCarValues {
    middleInUAH: number;
    middleInEUR: number;
    middleInUSD: number;
}

