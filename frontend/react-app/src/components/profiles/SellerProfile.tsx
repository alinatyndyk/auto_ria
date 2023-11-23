import React, {FC} from 'react';
import {ISellerResponse} from "../../interfaces/user/seller.interface";

interface IProps {
    seller: ISellerResponse
}

const SellerProfile: FC<IProps> = ({seller}) => {

    //todo add cars and pagination

    const {
        id, city, number, region, avatar, name, lastName, accountType, createdAt
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
                <img style={{height: "80px"}} src={`http://localhost:8080/users/avatar/${avatar}`} alt="Avatar"/>
                <div>{city} {region}</div>
                <div style={{fontSize: "9px"}}>{region}, {city}</div>
            </div>
            <div>{createdAt}</div>
            <br/>
        </div>
    );
};

export {SellerProfile};