import React, {FC} from 'react';
import {ICar} from "../../interfaces";

interface IProps {
    car: ICar
}

const Car: FC<IProps> = ({car}) => {

    const {
        id, city, currency,
        model, photo, powerH,
        price, priceEUR, priceUAH,
        priceUSD, region,
        seller,
        description, brand
    } = car;
    return (
        <div style={{
            display: "flex",
            backgroundColor: "whitesmoke",
            height: "110px", width: "220px",
            fontSize: "9px",
            columnGap: "10px"
        }}>
            <div>
                <img style={{height: "80px"}} src={`http://localhost:8080/users/avatar/${photo}`} alt="Avatar"/>
                <div>{price} {currency}</div>
                <div style={{fontSize: "9px"}}>{region}, {city}</div>
            </div>
            <div>
                <div>brand: {brand}</div>
                <div>model: {model}</div>
                <div>power (h): {powerH}</div>
            </div>
            <br/>
        </div>
    );
};

export {Car};