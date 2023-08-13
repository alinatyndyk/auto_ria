export interface ICar {
    id: number,
    brand: string,
    power: number,
    city: string,
    region: string,
    producer: string,
    photo: string[],
    seller: {
        id: number,
        name: string,
        email: string,
        avatar: string,
        city: string,
        password: string,
        roles: string[],
        region: string,
        number: number,
        accountType: string,

    },
    price: string,
    currency: string
}