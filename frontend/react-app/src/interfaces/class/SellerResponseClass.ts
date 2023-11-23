import {ISellerResponse} from "../user/seller.interface";

export class SellerResponse implements ISellerResponse {
    id: number = 0;
    name: string = '';
    lastName: string = '';
    city: string = '';
    region: string = '';
    number: string = '';
    avatar: string = '';
    accountType: string = '';
    createdAt: number[] = [];

    constructor(data?: ISellerResponse) {
        if (data) {
            this.id = data.id;
            this.name = data.name;
            this.lastName = data.lastName;
            this.city = data.city;
            this.region = data.region;
            this.number = data.number;
            this.avatar = data.avatar;
            this.accountType = data.accountType;
            this.createdAt = data.createdAt;
        }
    }
}