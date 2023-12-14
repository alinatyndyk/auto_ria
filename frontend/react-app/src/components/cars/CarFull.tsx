import React, {FC, useContext, useEffect, useState} from 'react';
import {Carousel} from "../Carousel";
import {useParams} from "react-router";
import {useAppDispatch, useAppNavigate, useAppSelector} from "../../hooks";
import {carActions} from "../../redux/slices";
import {ThemeContext} from "../../Context";
import {sellerActions} from "../../redux/slices/seller.slice";

import moment from "moment";
import {ERole} from "../../constants/role.enum";

// ...

const CarFull: FC = () => {

    const {carId} = useParams<{ carId: string }>();
    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();
    const [textButtonVisible, setTextButtonVisible] = useState(false);
    const [userByToken, setUserByToken] = useState();

    const {car} = useAppSelector(state => state.carReducer);
    const {user} = useAppSelector(state => state.sellerReducer);

    const theme = useContext(ThemeContext);

    useEffect(() => {
        if (!isNaN(Number(carId)) && Number(carId) > 0) {
            dispatch(carActions.getById(Number(carId)));
        }

        dispatch(sellerActions.getByToken());

        if (theme.role == ERole.CUSTOMER && theme.id != car?.seller.id) {
            setTextButtonVisible(true);
        }

    }, [carId]);

    if (car != null) {

        return (
            <div style={{
                // display: "flex",
                backgroundColor: "whitesmoke",
                fontSize: "9px",
                columnGap: "10px"
            }}>
                <div>
                    <Carousel images={car.photo.map((src, id) => ({
                        id,
                        src: `http://localhost:8080/users/avatar/${src}`,
                    }))}/>
                    <div>{car.price} {car.currency}</div>
                    <div style={{fontSize: "9px"}}>{car.region}, {car.city}</div>
                </div>
                <div>
                    <div>id: {car.id}</div>
                    <div>brand: {car.brand}</div>
                    <div>model: {car.model}</div>
                    <div>power (h): {car.powerH}</div>
                </div>
                <div>
                    <div>usd: {car.priceUSD}</div>
                    <div>eur: {car.priceEUR}</div>
                    <div>uah: {car.priceUAH}</div>
                </div>
                <div>desc: {car.description}</div>
                <div>seller: {JSON.stringify(car.seller)}</div>
                <div>{car.seller.createdAt}</div>
                <div>{moment(car.seller.createdAt).format("YYYY-MM-DD HH:mm:ss")}</div>
                {
                    textButtonVisible &&
                    <button onClick={() => navigate(`/chats/${car?.seller.id}`)}>Text Seller</button>
                }
                <br/>
            </div>
        );
    } else {
        return (
            <div>Error: invalid id</div>
        )
    }

};

export {CarFull};