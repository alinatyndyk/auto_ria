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