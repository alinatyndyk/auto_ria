import React, {FC, useState} from 'react';
import {ICar} from "../../interfaces";
import {Carousel} from "../Carousel";
import {useAppNavigate} from "../../hooks";

interface IProps {
    car: ICar
}

const Car: FC<IProps> = ({car}) => {

    const navigate = useAppNavigate();

    const {
        id, city, currency,
        model, photo, powerH,
        price, priceEUR, priceUAH,
        priceUSD, region,
        seller,
        description, brand
    } = car;

    return (
        <div onClick={() => navigate(`${id}`)} style={{
            display: "flex",
            backgroundColor: "whitesmoke",
            height: "130px", width: "220px",
            fontSize: "9px",
            columnGap: "10px"
        }}>
            <div>
                <div>id: {id}</div>
                <img height={"80px"} key={photo[0]} src={`http://localhost:8080/users/avatar/${photo[0]}`} alt=''/>
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