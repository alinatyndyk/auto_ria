import React, {FC} from 'react';
import {ISellerResponse} from "../../interfaces/user/seller.interface";
import {ICustomerResponse} from "../../interfaces/user/customer.interface";

interface IProps {
    seller: ICustomerResponse
}

const CustomerProfile: FC<IProps> = ({seller}) => {

    const {
        id, avatar, name, lastName, email //todo add created at to all
    } = seller;
    return (
        <div style={{
            display: "flex",
            backgroundColor: "whitesmoke",
            height: "110px", width: "220px",
            fontSize: "9px",
            columnGap: "10px"
        }}>
            <div>
                Customer
                <div>id: {id}</div>
                <div>{name} {lastName}</div>
                <div>{email}</div>
                <img style={{height: "80px"}} src={`http://localhost:8080/users/avatar/${avatar}`} alt="Avatar"/>
            </div>
            <br/>
        </div>
    );
};

export {CustomerProfile};