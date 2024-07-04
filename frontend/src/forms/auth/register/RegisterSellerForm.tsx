import React, { FC, useEffect, useState } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { useAppDispatch, useAppNavigate, useAppSelector } from '../../../hooks';
import { IAuthResponse, IError } from '../../../interfaces';
import { IGeoCity, IGeoRegion } from '../../../interfaces/geo.interface';
import { ISellerInput } from '../../../interfaces/user/seller.interface';
import { authActions } from '../../../redux/slices';
import { sellerActions } from '../../../redux/slices/seller.slice';
import './RegisterSellerForm.css'; // Import your CSS file for styling

const RegisterSellerForm: FC = () => {
    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();

    function isIAuthResponse(obj: any): obj is IAuthResponse {
        return 'accessToken' in obj && 'refreshToken' in obj;
    }

    const { reset, handleSubmit, register } = useForm<ISellerInput>();
    const { registerErrors } = useAppSelector(state => state.authReducer);
    const { regions, cities } = useAppSelector(state => state.sellerReducer);

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

    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');

    const registerSeller: SubmitHandler<ISellerInput> = async (seller: ISellerInput) => {
        try {
            if (!getRegionInput || !getCityInput) {
                setResponse('Please specify the region and city');
            } else {
                seller.city = getCarCity;
                seller.region = getCarRegion;

                if (code != null) {
                    seller.code = code;
                    const { payload } = await dispatch(authActions.registerUserAuth(seller));

                    if (isIAuthResponse(payload)) {
                        navigate('/profile');
                        setResponse(''); // Reset response state on successful navigation
                    } else {
                        setResponse(String(payload));
                    }
                } else {
                    const { payload, type } = await dispatch(authActions.registerSeller(seller));
                    const lastWord = type.substring(type.lastIndexOf('/') + 1);

                    if (lastWord == 'fulfilled') {
                        setResponse(String(payload));
                        reset();
                    } else {
                        const x = payload as IError;
                        setResponse(String(x.message));
                    }
                }

            }
        } catch (error) {
            setResponse(String(error));
        }
    };


    useEffect(() => {
        setRegions(regions);
    }, [regions]);

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
        <div className="carForm">
            <div className="form-header">Register user</div>
            {registerErrors ? (
                <div className="error-message">{registerErrors?.message}</div>
            ) : (
                <div className="error-message">{getResponse}</div>
            )}
            <form encType="multipart/form-data" onSubmit={handleSubmit(registerSeller)}>
                <div className="inputContainer">
                    <input type="text" placeholder="Name" {...register('name')} className="input" />
                </div>
                <div className="inputContainer">
                    <input type="text" placeholder="Last name" {...register('lastName')} className="input" />
                </div>
                <div className="inputContainer">
                    <input
                        placeholder="Region"
                        {...register('region')}
                        value={getCarRegion}
                        disabled={getRegionInput}
                        autoComplete="off"
                        type="text"
                        onChange={handleInputChange}
                        className="input"
                    />
                    <button
                        type="button"
                        onClick={() => {
                            setRegionInput(false);
                            setCarRegion('');
                            setIsRegionVisible(true);
                        }}
                        className="changeButton"
                    >
                        Change region
                    </button>
                </div>
                {isRegionVisible && (
                    <div className="inputContainer columnContainer">
                        {getRegions.map((region) => (
                            <div key={region.isoCode} onClick={() => handleRegionClick(region)} className="columnItem">
                                {region.name}
                            </div>
                        ))}
                    </div>
                )}
                <div className="inputContainer">
                    <input
                        placeholder="City"
                        {...register('city')}
                        value={getCarCity}
                        disabled={getCityInput}
                        autoComplete="off"
                        type="text"
                        onChange={handleCityInputChange}
                        className="input"
                    />
                    <button
                        type="button"
                        onClick={() => {
                            setCityInput(false);
                            setCarCity('');
                            setIsCityVisible(true);
                        }}
                        className="changeButton"
                    >
                        Change city
                    </button>
                </div>
                {isCityVisible && (
                    <div className="inputContainer columnContainer">
                        {getCities.map((city) => {
                            if (city.name.toLowerCase().startsWith(getCityInputValue.toLowerCase())) {
                                return (
                                    <div
                                        key={city.name}
                                        onClick={() => handleCityClick(city.name)}
                                        className="columnItem"
                                    >
                                        {city.name}
                                    </div>
                                );
                            }
                            return null;
                        })}
                    </div>
                )}
                <div className="inputContainer">
                    <input type="text" placeholder="Email" {...register('email')} className="input" />
                </div>
                <div className="inputContainer">
                    <input type="text" placeholder="Number" {...register('number')} className="input" />
                </div>
                <div className="inputContainer">
                    <input type="text" placeholder="Password" {...register('password')} className="input" />
                </div>
                <div className="inputContainer">
                    <input formEncType="multipart/form-data" type="file" placeholder="Avatar" {...register('avatar')} className="input" />
                </div>
                <button type="submit" className="submitButton">Register</button>
            </form>
        </div>
    );
};

export { RegisterSellerForm };
