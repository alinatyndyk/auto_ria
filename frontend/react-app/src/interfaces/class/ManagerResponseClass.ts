import {IManagerResponse} from "../user/manager.interface";

export class ManagerResponse implements IManagerResponse {
    id: number = 0;
    name: string = '';
    lastName: string = '';
    email: string = '';
    avatar: File | null = null;

    constructor(data?: IManagerResponse) {
        if (data) {
            this.id = data.id;
            this.name = data.name;
            this.lastName = data.lastName;
            this.email = data.email;
            this.avatar = data.avatar;
        }
    }
}