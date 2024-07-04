import React, { FC, useEffect, useState } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { useAppDispatch, useAppNavigate, useAppSelector } from '../../../../hooks';
import { IGeoCity, IGeoRegion } from '../../../../interfaces/geo.interface';
import { IUserUpdateRequest } from '../../../../interfaces/user/seller.interface';
import { sellerActions } from '../../../../redux/slices/seller.slice';
import './UpdateUserForm.css'; // Import your CSS file

const UpdateUserForm: FC = () => {
    const { reset, handleSubmit, register } = useForm<IUserUpdateRequest>();
    const dispatch = useAppDispatch();

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
    const { regions, cities, errorUpdateById } = useAppSelector(state => state.sellerReducer);

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

    const { user } = useAppSelector(state => state.sellerReducer);

    const save: SubmitHandler<IUserUpdateRequest> = async (userToUpdate: IUserUpdateRequest) => {
        userToUpdate.region = getCarRegion;
        userToUpdate.city = getCarCity;

        const updatedUser: Partial<IUserUpdateRequest> = {};

        Object.keys(userToUpdate).forEach(key => {
            const value = userToUpdate[key as keyof IUserUpdateRequest];
            if (value !== undefined && value !== null && value !== '') {
                updatedUser[key as keyof IUserUpdateRequest] = value;
            }
        });

        if (user !== null) {
            await dispatch(sellerActions.updateById({ id: user.id, body: updatedUser }))
                .then((res) => {
                    const type = res.type;
                    const lastWord = type.substring(type.lastIndexOf("/") + 1);

                    if (lastWord === "fulfilled") {
                        setResponse("User updated successfully");
                        setCarCity('');
                        setCarRegion('');
                        reset();
                    }
                });
        }
    };

    return (
        <div className="update-user-form">
            <div>Change account info</div>
            <form onSubmit={handleSubmit(save)}>
                <div className="inputContainer">
                    <div>{errorUpdateById?.message ? errorUpdateById.message : <div>{getResponse}</div>}</div>
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
                    <input className="input" autoComplete={"off"} type="text"
                        placeholder={'number'} {...register('number')} />
                </div>
                <div className="inputContainer">
                    <input className="input" autoComplete={"off"} type="text"
                        placeholder={'name'} {...register('name')} />
                </div>
                <div className="inputContainer">
                    <input className="input" autoComplete={"off"} type="text"
                        placeholder={'lastName'} {...register('lastName')} />
                </div>
                <button className="submitButton">Update account information</button>
            </form>
        </div>
    );
};

export {UpdateUserForm};
