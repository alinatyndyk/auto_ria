import { FC } from 'react';
import { CarForm } from '../../forms/car/CarForm';
import { IUserResponse } from "../../interfaces/user/seller.interface";
import { Cars } from "../cars";
import { StripeCheckout } from "../stripe/StripeCheckout";

interface IProps {
    seller: IUserResponse
}

const SellerProfile: FC<IProps> = ({ seller }) => {

    const {
        id, city, number, region, avatar, name, lastName, accountType, createdAt
    } = seller;

    let picture;
    if (avatar === null) {
        picture = "channels4_profile.jpg";
    } else {
        picture = avatar;
    }

    const date = createdAt.slice(0, 3);
    const formattedNumbers = `${date[0]}.${date[1]}.${date[2]}`;

    return (
        <div>
            <div style={{ display: "flex", columnGap: "20px" }}>
                <img style={{ height: "80px", width: "80px", borderRadius: "50%", marginRight: "10px" }}
                    src={`http://localhost:8080/users/avatar/${picture}`} alt="Avatar" />
                <div>

                    <div>id: {id}</div>
                    <div>{name} {lastName}</div>
                    <div>✆ {number}</div>
                    <div style={{ fontSize: "9px" }}>◉ {region}, {city}</div>
                    <div>account: {accountType}</div>
                    <div>joined: {formattedNumbers}</div>
                    <div style={{ width: "500px" }}>
                        <StripeCheckout seller={seller} />
                    </div>
                </div>
                <CarForm />
            </div>
            <br />
            <Cars sellerId={id} />

        </div>
    );
};

export { SellerProfile };
