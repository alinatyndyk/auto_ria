import React, {FC} from 'react';
import {ISellerResponse} from "../../interfaces/user/seller.interface";
import {ICustomerResponse} from "../../interfaces/user/customer.interface";
import {ChatPage} from "../../pages/ChatPage";

interface IProps {
    seller: ICustomerResponse
}

const CustomerProfile: FC<IProps> = ({seller}) => {

    const {
        id, avatar, name, lastName
    } = seller;
    let picture;
    if (avatar == null) {
        picture = "channels4_profile.jpg";
    } else {
        picture = avatar;
    }
    return (
        <div>

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
                    <img style={{height: "80px", width: "80px", borderRadius: "50%", marginRight: "10px"}}
                         src={`http://localhost:8080/users/avatar/${picture}`} alt="Avatar"/></div>
                <br/>
            </div>
            <ChatPage/>
        </div>
    );
};

export {CustomerProfile};