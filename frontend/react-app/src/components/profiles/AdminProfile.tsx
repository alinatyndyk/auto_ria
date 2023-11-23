import React, {FC} from 'react';
import {ISellerResponse} from "../../interfaces/user/seller.interface";
import {IAdminResponse} from "../../interfaces/user/admin.interface";

interface IProps {
    seller: IAdminResponse
}

const AdminProfile: FC<IProps> = ({seller}) => {

    const {
        id, avatar, name, lastName, email
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
                Seller
                <div>id: {id}</div>
                <div>{name} {lastName}</div>
                <div>{email}</div>
                <img style={{height: "80px"}} src={`http://localhost:8080/users/avatar/${avatar}`} alt="Avatar"/>
            </div>
            <br/>
        </div>
    );
};

export {AdminProfile};