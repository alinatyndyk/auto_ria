import React, { FC, useEffect, useState } from 'react';
import { useParams } from "react-router";
import { useAppDispatch, useAppNavigate, useAppSelector } from "../../hooks";
import { carActions } from "../../redux/slices";
import { Carousel } from "./Carousel";

import moment from "moment";
import { SubmitHandler, useForm } from "react-hook-form";
import { ERole } from "../../constants/role.enum";
import { ECurrency } from '../../forms/car/CarForm';
import { ICreateInputCar, IUpdateInputCar } from "../../interfaces";
import { IGeoCity, IGeoRegion } from "../../interfaces/geo.interface";
import { sellerActions } from "../../redux/slices/seller.slice";
import { authService } from '../../services';

const CarFull: FC = () => {

    const { carId } = useParams<{ carId: string }>();
    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();
    const { reset, handleSubmit, register } = useForm<ICreateInputCar>();

    const { car, errorGetMiddle, middleValue } = useAppSelector(state => state.carReducer);
    const { userAuthotization } = useAppSelector(state => state.sellerReducer);

    const [getRegions, setRegions] = useState<IGeoRegion[]>([]);
    const [getCities, setCities] = useState<IGeoCity[]>([]);

    const [getBanResponse, setBanResponse] = useState('');

    const [isRegionVisible, setIsRegionVisible] = useState(true);
    const [isCityVisible, setIsCityVisible] = useState(true);

    const [getCarRegion, setCarRegion] = useState('');
    const [getCarRegionId, setCarRegionId] = useState('');

    const [getCityInputValue, setCityInputValue] = useState('');
    const [getCarCity, setCarCity] = useState('');

    const [getRegionInput, setRegionInput] = useState(false);
    const [getCityInput, setCityInput] = useState(true);

    const { errorUpdateById, errorDeleteById, errorGetById } = useAppSelector(state => state.carReducer);

    const [isCurrencyVisible, setIsCurrencyVisible] = useState(false);
    const [getCurrency, setCurrency] = useState<ECurrency>(ECurrency.EUR);
    const [getCurrencies, setCurrencies] = useState<ECurrency[]>([]);
    const [getResponse, setResponse] = useState('');
    const { regions, cities } = useAppSelector(state => state.sellerReducer);

    useEffect(() => {
        setRegions(regions);
    }, [regions])

    useEffect(() => {
        setCities(cities);
    }, [cities]);

    useEffect(() => {
        setCurrencies(Object.values(ECurrency));
    }, [])

    useEffect(() => {
        if (!isNaN(Number(carId)) && Number(carId) > 0) {
            dispatch(carActions.getById(Number(carId)));
            dispatch(carActions.getMiddleById(Number(carId)));
        }

    }, []);

    const { user } = useAppSelector(state => state.sellerReducer);

    useEffect(() => {
        const token = authService.getAccessToken();
        if (token != null) {
            dispatch(sellerActions.getByToken());
        }
    }, [])

    const deleteCar = (carId: number) => {
        dispatch(carActions.deleteById(carId));
        if (!errorDeleteById) {
            navigate("/profile");
        }
    }

    const banCar = async (carId: number) => {
        const { payload } = await dispatch(carActions.banById(carId));
        const response = JSON.stringify(payload);
        setBanResponse(response);
    }

    const unbanCar = async (carId: number) => {
        const { payload } = await dispatch(carActions.unbanById(carId));
        const response = JSON.stringify(payload);
        setBanResponse(response);
    }

    const handleInputChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        if (!getRegionInput) setCarRegion(event.target.value);
        await dispatch(sellerActions.getRegionsByPrefix(event.target.value));
    };


    const handleRegionClick = (region: IGeoRegion) => {
        setCarRegion(region.name);
        setCarRegionId(region.isoCode);
        setRegionInput(true);
        setCityInput(false);
        setIsRegionVisible(false);
    };

    const handleCityInputChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        if (!getCityInput) setCarCity(event.target.value);
        setCityInputValue(event.target.value);
        await dispatch(sellerActions.getRegionsPlaces(getCarRegionId));
    };


    const handleCityClick = (cityName: string) => {
        setCarCity(cityName);
        setCityInput(true);
        setIsCityVisible(false);
    };


    const save: SubmitHandler<IUpdateInputCar> = async (car: IUpdateInputCar) => {
        car.region = getCarRegion;
        car.city = getCarCity;
        car.currency = getCurrency;

        const updatedCar: Partial<IUpdateInputCar> = {};

        Object.keys(car).forEach(key => {
            const value = car[key as keyof IUpdateInputCar];
            if (value !== undefined && value !== null && value !== '') {
                updatedCar[key as keyof IUpdateInputCar] = value;
            }
        });

        await dispatch(carActions.update({ car: updatedCar, id: Number(carId) }))
            .then((res) => {
                const type = res.type;
                const lastWord = type.substring(type.lastIndexOf("/") + 1);

                if (lastWord === "fulfilled") {
                    setResponse("Car updated successfully");
                    setCarCity('');
                    setCarRegion('');
                    reset();
                }
            });
    };


    if (car != null) {
        return (
            <div style={{
                backgroundColor: "whitesmoke",
                fontSize: "9px",
                columnGap: "10px"
            }}>
                <div>{JSON.stringify(user)} --  the user</div>
                <div> {car.photo.length > 0 ? <Carousel images={car.photo.map((src, id) => ({
                    id,
                    src: `http://localhost:8080/users/avatar/${src}`,
                }))} /> : null}
                    <div>{car.price} {car.currency}</div>
                    <div style={{ fontSize: "9px" }}>{car.region}, {car.city}</div>
                </div>
                <div>
                    <div>id: {car.id}</div>
                    {userAuthotization && userAuthotization.id === car?.user.id &&
                        <button onClick={() => deleteCar(car?.id)}>delete</button>}
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
                <div>seller: {JSON.stringify(car.user)}</div>
                <div>{car.user.createdAt}</div>
                <div>{moment(car.user.createdAt).format("YYYY-MM-DD HH:mm:ss")}</div>
                {car?.user.role === ERole.ADMIN && <div style={{ color: "blue" }}>The car is sold by AutoRio Services.
                    Please use {car?.user.number} for further information</div>}
                <br />
                {userAuthotization && (userAuthotization.id === car.user.id || userAuthotization.role === ERole.ADMIN) &&

                    <form onSubmit={handleSubmit(save)}>
                        <div>
                            <div>{errorUpdateById ? errorUpdateById?.message : <div>{getResponse}</div>}</div>
                            <input placeholder={'region'} {...register('region', { value: getCarRegion })}
                                value={getCarRegion} disabled={getRegionInput}
                                autoComplete={"off"} type="text" onChange={handleInputChange} />
                            <button onClick={() => {
                                setRegionInput(false);
                                setCarRegion('');
                                setIsRegionVisible(true);
                                setCarCity('');
                            }}>change region
                            </button>
                        </div>
                        {isRegionVisible &&
                            <div>
                                {getRegions.map((region) => (
                                    <div key={region.isoCode} onClick={() => {
                                        handleRegionClick(region);
                                        setResponse('');
                                    }}>
                                        {region.name}
                                    </div>
                                ))}
                            </div>
                        }
                        <div>
                            <input placeholder={'city'} {...register('city', { value: getCarCity })}
                                value={getCarCity} disabled={getCityInput}
                                autoComplete={"off"} type="text" onChange={handleCityInputChange} />
                            <button onClick={() => {
                                setCityInput(false);
                                setCarCity('');
                                setIsCityVisible(true);
                            }}>change city
                            </button>
                        </div>
                        {isCityVisible &&
                            <div>
                                {isCityVisible && getCities.map((city) => {
                                    if (city.name.toLowerCase().startsWith(getCityInputValue.toLowerCase())) {
                                        return (
                                            <div
                                                key={city.name}
                                                onClick={() => {
                                                    handleCityClick(city.name);
                                                    setResponse('');
                                                }}>
                                                {city.name}
                                            </div>
                                        );
                                    }
                                })}
                            </div>
                        }
                        <div>
                            <input type="number" placeholder={'price'} {...register('price')} />
                        </div>
                        <div>
                            <input autoComplete={"off"} type="text" readOnly={true} value={getCurrency}
                                placeholder={'currency'} {...register('currency', { value: getCurrency })}
                                onClick={() => {
                                    setIsCurrencyVisible(true);
                                }} />
                        </div>
                        {isCurrencyVisible && getCurrencies.map((curr) => {
                            return (
                                <div
                                    key={curr}
                                    onClick={() => {
                                        setCurrency(curr);
                                        setIsCurrencyVisible(false);
                                    }}>
                                    {curr}
                                </div>
                            );
                        })}
                        <div>
                            <input autoComplete={"off"} type="text"
                                placeholder={'description'} {...register('description')} />
                        </div>
                        <button>update</button>
                    </form>
                }
                <div>
                    <div>
                        {userAuthotization && (userAuthotization.role === ERole.MANAGER
                            || userAuthotization.role === ERole.ADMIN
                            || userAuthotization.id === car.user.id) && (
                                <div>
                                    <div>Middle price in the region - premium</div>
                                    <div>{errorGetMiddle?.message ? errorGetMiddle.message : null}</div>
                                    <div>Middle in EUR: {middleValue?.middleInEUR}</div>
                                    <div>Middle in UAH: {middleValue?.middleInUAH}</div>
                                    <div>Middle in USD: {middleValue?.middleInUSD}</div>
                                </div>
                            )}
                    </div>
                    {userAuthotization && (userAuthotization.role === ERole.MANAGER || userAuthotization.role === ERole.ADMIN) && (
                        car.isActivated ? (
                            <div>
                                {getBanResponse ? getBanResponse : null}
                                <button style={{ backgroundColor: "red", color: "white" }} onClick={() => banCar(car?.id)}>ban car</button>
                            </div>
                        ) : (
                            <div>
                                {getBanResponse ? getBanResponse : null}
                                <button style={{ backgroundColor: "green", color: "white" }} onClick={() => unbanCar(car?.id)}>unban car</button>
                            </div>
                        )
                    )}
                </div>
            </div>
        );
    } else {
        return (
            <div>
                error fetching car
            </div>
        )
    }

};

export { CarFull };

