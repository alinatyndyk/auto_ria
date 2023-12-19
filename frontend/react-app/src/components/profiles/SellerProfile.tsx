import React, {FC} from 'react';
import {ISellerResponse} from "../../interfaces/user/seller.interface";
import {CarForm, Cars} from "../cars";
import {StripeCheckout} from "../stripe/StripeCheckout";
import {ChatPage} from "../../pages/chat/ChatPage";

interface IProps {
    seller: ISellerResponse
}

const SellerProfile: FC<IProps> = ({seller}) => {

    const {
        id, city, number, region, avatar, name, lastName, accountType, createdAt
    } = seller;

    let picture;
    if (avatar == null) {
        picture = "channels4_profile.jpg";
    } else {
        picture = avatar;
    }

    return (
        <div>
            <div style={{display: "flex", columnGap: "20px"}}>
                <img style={{height: "80px", width: "80px", borderRadius: "50%", marginRight: "10px"}}
                     src={`http://localhost:8080/users/avatar/${picture}`} alt="Avatar"/>
                <div>

                    <div>id: {id}</div>
                    <div>{name} {lastName}</div>
                    <div>✆ {number}</div>
                    <div style={{fontSize: "9px"}}>◉ {region}, {city}</div>
                    <div>account: {accountType}</div>
                    <div>joined: {createdAt}</div>

                </div>
                <StripeCheckout seller={seller}/>
            </div>
            <br/>
            <CarForm/>
            <ChatPage/>
            <Cars sellerId={id}/>
        </div>
    );
};

export {SellerProfile};