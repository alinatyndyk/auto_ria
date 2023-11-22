export interface ICustomerInput {
    name: string,
    lastName: string,
    city: string,
    region: string,
    email: string,
    password: string,
    avatar: File | null
}