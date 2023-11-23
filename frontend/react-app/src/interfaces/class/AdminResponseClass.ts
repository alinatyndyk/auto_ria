import {IAdminResponse} from "../user/admin.interface";

export class AdminResponse implements IAdminResponse {
    id: number = 0;
    name: string = '';
    lastName: string = '';
    email: string = '';
    avatar: File | null = null;

    constructor(data?: IAdminResponse) {
        if (data) {
            this.id = data.id;
            this.name = data.name;
            this.lastName = data.lastName;
            this.email = data.email;
            this.avatar = data.avatar;
        }
    }
}