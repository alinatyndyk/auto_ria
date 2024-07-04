import React, { FC, useEffect, useState } from 'react';
import { SubmitHandler, useForm } from "react-hook-form";
import { useAppDispatch, useAppNavigate, useAppSelector } from "../../hooks";
import { ICreateInputCar, IUpdateInputCar } from "../../interfaces";
import { IGeoCity, IGeoRegion } from "../../interfaces/geo.interface";
import { carActions } from "../../redux/slices";
import { sellerActions } from "../../redux/slices/seller.slice";
import './CarUpdateForm.css';
import { authService } from '../../services';
import { useParams } from 'react-router-dom';

export enum ECurrency {
    UAH = "UAH", EUR = "EUR", USD = "USD"
}

const CarUpdateForm: FC = () => {
    const { carId } = useParams<{ carId: string }>();
    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();
    const { reset, handleSubmit, register } = useForm<ICreateInputCar>();

    const [getRegions, setRegions] = useState<IGeoRegion[]>([]);
    const [getCities, setCities] = useState<IGeoCity[]>([]);

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


    return (
        <form className="carForm" onSubmit={handleSubmit(save)}>
            <div className="inputContainer">
                <div>{errorUpdateById ? errorUpdateById?.message : <div>{getResponse}</div>}</div>
            </div>
            <div className="inputContainer">
                <input className="input" placeholder={'region'} {...register('region', { value: getCarRegion })}
                    value={getCarRegion} disabled={getRegionInput}
                    autoComplete={"off"} type="text" onChange={handleInputChange} />
                <button type="button" className="changeButton" onClick={() => {
                    setRegionInput(false);
                    setCarRegion('');
                    setIsRegionVisible(true);
                    setCarCity('');
                }}>change region
                </button>
            </div>
            {isRegionVisible &&
                <div className="dropdown">
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
            <div className="inputContainer">
                <input className="input" placeholder={'city'} {...register('city', { value: getCarCity })}
                    value={getCarCity} disabled={getCityInput}
                    autoComplete={"off"} type="text" onChange={handleCityInputChange} />
                <button type="button" className="changeButton" onClick={() => {
                    setCityInput(false);
                    setCarCity('');
                    setIsCityVisible(true);
                }}>change city
                </button>
            </div>
            {isCityVisible &&
                <div className="dropdown">
                    {getCities.map((city) => {
                        if (city.name.toLowerCase().startsWith(getCityInputValue.toLowerCase())) {
                            return (
                                <div key={city.name} onClick={() => {
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
            <div className="inputContainer">
                <input className="input" type="number" placeholder={'price'} {...register('price')} />
            </div>
            <div className="inputContainer">
                <input className="input" autoComplete={"off"} type="text" readOnly={true} value={getCurrency}
                    placeholder={'currency'} {...register('currency', { value: getCurrency })}
                    onClick={() => {
                        setIsCurrencyVisible(true);
                    }} />
            </div>
            {isCurrencyVisible && getCurrencies.map((curr) => {
                return (
                    <div key={curr} onClick={() => {
                        setCurrency(curr);
                        setIsCurrencyVisible(false);
                    }}>
                        {curr}
                    </div>
                );
            })}
            <div className="inputContainer">
                <input className="input" autoComplete={"off"} type="text"
                    placeholder={'description'} {...register('description')} />
            </div>
            <button className="submitButton">Update car information</button>
        </form>
    );
};

export { CarUpdateForm };
