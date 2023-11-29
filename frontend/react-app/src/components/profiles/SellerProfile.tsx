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

    console.log(seller, "seller")

    let page = 0;

    let picture;
    if (avatar == null) {
        picture = "channels4_profile.jpg";
    } else {
        picture = avatar;
    }

    return (
        <div>
            <div style={{display: "flex"}}>
                <img style={{height: "80px", width: "80px", borderRadius: "50%", marginRight: "10px"}}
                     src={`http://localhost:8080/users/avatar/${picture}`} alt="Avatar"/>
                <div>

                    <div>id: {id}</div>
                    <div>{name} {lastName}</div>
                    <div>{number}</div>
                    <div>account: {accountType}</div>
                    <div style={{fontSize: "9px"}}>{region}, {city}</div>
                    <div>joined: {createdAt}</div>

                </div>
            </div>
            <br/>
            <StripeCheckout seller={seller}/>
            <Cars sellerId={id}/>
        </div>
    );
};

export {SellerProfile};