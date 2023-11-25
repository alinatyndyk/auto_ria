import React, {FC} from 'react';
import {ISellerResponse} from "../../interfaces/user/seller.interface";
import {useAppDispatch, useAppNavigate} from "../../hooks";
import {Cars} from "../cars";
import {StripeCheckout} from "../stripe/StripeCheckout";

interface IProps {
    seller: ISellerResponse
}

const SellerProfile: FC<IProps> = ({seller}) => {

    //todo add cars and pagination
    //todo normal timestamp

    const navigate = useAppNavigate();
    const dispatch = useAppDispatch();

    const {
        id, city, number, region, avatar, name, lastName, accountType, createdAt
    } = seller;

    let page = 0;

    return (
        <div>
            <div>
                <div>Seller</div>
                <img style={{height: "80px"}} src={`http://localhost:8080/users/avatar/${avatar}`} alt="Avatar"/>
                <div>id: {id}</div>
                <div>{name} {lastName}</div>
                <div>{number}</div>
                <div>type: {accountType}</div>
                <div style={{fontSize: "9px"}}>{region}, {city}</div>

            </div>
            <div>{createdAt}</div>
            <br/>
            <StripeCheckout seller={seller}/>
            <Cars sellerId={id}/>
        </div>
    );
};

export {SellerProfile};