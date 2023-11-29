import React, {FC, useEffect, useState} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {useAppDispatch, useAppSelector} from "../../../hooks";
import {ISellerInput} from "../../../interfaces/user/seller.interface";
import {authActions} from "../../../redux/slices";
import {sellerActions} from "../../../redux/slices/seller.slice";
import {IGeoCity, IGeoRegion} from "../../../interfaces/geo.interface";

const RegisterSellerForm: FC = () => {
    const dispatch = useAppDispatch();

    const {reset, handleSubmit, register} = useForm<ISellerInput>();
    const {errors} = useAppSelector(state => state.authReducer);
    const {regions, cities} = useAppSelector(state => state.sellerReducer);

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

    const [getResponse, setResponse] = useState('');

    const registerSeller: SubmitHandler<ISellerInput> = async (seller: ISellerInput) => {
        if (!getRegionInput || !getCityInput) {
            setResponse("Please specify the region and city");
        } else {
            const {payload} = await dispatch(authActions.registerSeller(seller));
            setResponse(String(payload));
            reset();
        }

    }

    useEffect(() => {
        setRegions(regions);
    }, [regions])

    useEffect(() => {
        setCities(cities);
    }, [cities]);


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


    return (
        <div>
            Register a seller
            {errors ? <div>{errors?.message}</div> : <div>{getResponse}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(registerSeller)}>
                <div>
                    <input type="text" placeholder={'name'} {...register('name')}/>
                </div>
                <div>
                    <input type="text" placeholder={'last name'} {...register('lastName')}/>
                </div>
                <div>
                    <input placeholder={'region'} {...register('region')} value={getCarRegion} disabled={getRegionInput}
                           autoComplete={"off"} type="text" onChange={handleInputChange}/>
                    <button onClick={() => {
                        setRegionInput(false);
                        setCarRegion('');
                        setIsRegionVisible(true);
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
                    <input placeholder={'city'} {...register('city')} value={getCarCity} disabled={getCityInput}
                           autoComplete={"off"} type="text" onChange={handleCityInputChange}/>
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
                    <input type="text" placeholder={'email'} {...register('email')}/>
                </div>
                <div>
                    <input type="text" placeholder={'number'} {...register('number')}/>
                </div>
                <div>
                    <input type="text" placeholder={'password'} {...register('password')}/>
                </div>
                <div>
                    <input formEncType="multipart/form-data" type="file"
                           placeholder={'avatar'} {...register('avatar')}/>
                </div>
                <button>Register</button>
            </form>
        </div>
    );
};

export {RegisterSellerForm};