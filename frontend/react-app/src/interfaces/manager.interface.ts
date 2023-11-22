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