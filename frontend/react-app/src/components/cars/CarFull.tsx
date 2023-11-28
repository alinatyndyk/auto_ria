import React, {FC, useState} from 'react';
import {ICar} from "../../interfaces";

interface IProps {
    car: ICar
}

const CarFull: FC<IProps> = ({car}) => {

    const [getPhotos, setPhotos] = useState();

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
            {

                // photo?.map(image => <img key={image} src={`http://localhost:8080/users/avatar/${image}`} alt=''/>)
            }
        </div>
    );
};

export {CarFull};