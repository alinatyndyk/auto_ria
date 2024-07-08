import { faCar, faClock, faDollarSign, faEuroSign, faHorseHead, faHryvnia, faInfoCircle, faLocationArrow, faUser } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { FC, useEffect, useState } from 'react';
import { useParams } from "react-router";
import { ERole } from "../../constants/role.enum";
import { CarUpdateForm } from '../../forms/car/CarUpdateForm';
import { useAppDispatch, useAppNavigate, useAppSelector } from "../../hooks";
import { IUserResponse } from '../../interfaces/user/seller.interface';
import ErrorForbidden from '../../pages/error/ErrorForbidden';
import { carActions } from "../../redux/slices";
import { sellerActions } from "../../redux/slices/seller.slice";
import { authService } from '../../services';
import LoadingPage from '../LoadingPage';
import './CarFull.css';
import { Carousel } from "./Carousel";

const CarFull: FC = () => {
    const { carId } = useParams<{ carId: string }>();
    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();

    const { car, errorGetMiddle, middleValue, isCarLoading } = useAppSelector(state => state.carReducer);
    const { userAuthotization } = useAppSelector(state => state.sellerReducer);

    const [getBanResponse, setBanResponse] = useState('');

    const { errorDeleteById } = useAppSelector(state => state.carReducer);

    useEffect(() => {
        if (!isNaN(Number(carId)) && Number(carId) > 0) {
            dispatch(carActions.getById(Number(carId)));
            dispatch(carActions.getMiddleById(Number(carId)));
        }
    }, [dispatch, carId]);

    useEffect(() => {
        const token = authService.getAccessToken();
        if (token != null) {
            dispatch(sellerActions.getByToken());
        }
    }, [dispatch]);

    const deleteCar = (carId: number) => {
        dispatch(carActions.deleteById(carId));
        if (!errorDeleteById) {
            navigate("/profile");
        }
    };

    const banCar = async (carId: number) => {
        const { payload } = await dispatch(carActions.banById(carId));
        const response = JSON.stringify(payload);
        setBanResponse(response);
    };

    const unbanCar = async (carId: number) => {
        const { payload } = await dispatch(carActions.unbanById(carId));
        const response = JSON.stringify(payload);
        setBanResponse(response);
    };

    if (isCarLoading) {
        return <LoadingPage />;
    }

    if (!car) {
        return <ErrorForbidden cause='Car doesn`t exist or is banned' />;
    }

    const date = car.user.createdAt.slice(0, 3);
    const formattedNumbers = `${date[0]}.${date[1]}.${date[2]}`;


    return (
        <div className="carFull">
            <div className="carFull__carousel">
                {car.photo.length > 0 ? <Carousel images={car.photo.map((src, id) => ({
                    id,
                    src: `http://localhost:8080/users/avatar/${src}`,
                }))} /> : null}
                <br />
                {userAuthotization && (userAuthotization.id === car.user.id || userAuthotization.role === ERole.ADMIN) &&
                    <div className="carFull__updateForm">
                        <CarUpdateForm />
                    </div>
                }
            </div>
            <div className="carFull__info">
                <div className="carFull__price"><FontAwesomeIcon icon={faDollarSign} /> {car.price} {car.currency}</div>
                <div className="carFull__location"><FontAwesomeIcon icon={faLocationArrow} /> {car.region}, {car.city}</div>
                <div className="carFull__details">
                    {userAuthotization && ((userAuthotization.role === ERole.USER || userAuthotization.role === ERole.ADMIN || userAuthotization.id === car?.user.id)) &&
                        <button className="carFull__deleteButton" onClick={() => deleteCar(car?.id)}>delete</button>}
                    <div><FontAwesomeIcon icon={faCar} /> brand: {car.brand}</div>
                    <div><FontAwesomeIcon icon={faCar} /> model: {car.model}</div>
                    <div><FontAwesomeIcon icon={faHorseHead} /> power (h): {car.powerH}</div>
                </div>
                <div className="carFull__pricesBox">
                    <div className="carFull__prices">
                        <div><FontAwesomeIcon icon={faDollarSign} /> usd: {car.priceUSD}</div>
                        <div><FontAwesomeIcon icon={faEuroSign} /> eur: {car.priceEUR}</div>
                        <div><FontAwesomeIcon icon={faHryvnia} /> uah: {car.priceUAH}</div>
                    </div>
                </div>
                <div className="carFull__desc">
                    <div><FontAwesomeIcon icon={faInfoCircle} /> desc: {car.description}</div>
                    <div><FontAwesomeIcon icon={faUser} /> seller: {car.user.name + " " + car.user.lastName}</div>
                    <div><FontAwesomeIcon icon={faClock} /> {formattedNumbers}</div>
                </div>
                {car?.user.role === ERole.ADMIN && <div style={{ color: "blue" }}>The car is sold by AutoRio Services.
                    Please use {car?.user.number} for further information</div>}
                {userAuthotization && (userAuthotization.role === ERole.MANAGER || userAuthotization.role === ERole.ADMIN || userAuthotization.accountType == 'PREMIUM') && (
                    <div className="carFull__middleValueBox">
                        <div>Middle price in the region - premium</div>
                        <div>{errorGetMiddle?.message ? errorGetMiddle.message : null}</div>
                        <div><FontAwesomeIcon icon={faDollarSign} /> usd: {middleValue?.middleInUSD}</div>
                        <div><FontAwesomeIcon icon={faEuroSign} />eur: {middleValue?.middleInEUR}</div>
                        <div><FontAwesomeIcon icon={faHryvnia} /> uah: {middleValue?.middleInUAH}</div>
                    </div>
                )}
                {userAuthotization && (userAuthotization.role === ERole.MANAGER || userAuthotization.role === ERole.ADMIN) && (
                    car.isActivated ? (
                        <div>
                            {getBanResponse ? getBanResponse : null}
                            <button className="carFull__deleteButton" style={{ backgroundColor: "red", color: "white" }} onClick={() => banCar(car?.id)}>ban car</button>
                        </div>
                    ) : (
                        <div>
                            {getBanResponse ? getBanResponse : null}
                            <button className="carFull__unbanButton" style={{ backgroundColor: "green", color: "white" }} onClick={() => unbanCar(car?.id)}>unban car</button>
                        </div>
                    )
                )}
            </div>
        </div>
    );
};

export { CarFull };

