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
        number: string;
        avatar: string | null;
        createdAt: number[];
    };
    priceUAH: number;
    priceEUR: number;
    priceUSD: number;
}

interface Sort {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
}

interface Pageable {
    sort: Sort;
    offset: number;
    pageSize: number;
    pageNumber: number;
    unpaged: boolean;
    paged: boolean;
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