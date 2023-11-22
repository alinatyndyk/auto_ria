export interface ISellerInput {
    name: string,
    lastName: string,
    city: string,
    region: string,
    email: string,
    number: string,
    password: string,
    avatar: File
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
    createdAt: string;
}