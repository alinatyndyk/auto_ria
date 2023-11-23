import {ICustomerResponse} from "../user/customer.interface";

export class CustomerResponse implements ICustomerResponse {
    id: number = 0;
    name: string = '';
    lastName: string = '';
    email: string = '';
    avatar: File | null = null;

    constructor(data?: ICustomerResponse) {
        if (data) {
            this.id = data.id;
            this.name = data.name;
            this.lastName = data.lastName;
            this.email = data.email;
            this.avatar = data.avatar;
        }
    }
}