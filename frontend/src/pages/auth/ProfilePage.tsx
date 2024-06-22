import { FC, useEffect, useState } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { AdminProfile } from "../../components/profiles/AdminProfile";
import { ManagerProfile } from "../../components/profiles/ManagerProfile";
import { SellerProfile } from "../../components/profiles/SellerProfile";
import { ERole } from "../../constants/role.enum";
import { ChangePasswordForm } from "../../forms/auth/passwords/ChangePasswordForm";
import { useAppDispatch, useAppSelector } from "../../hooks";
import { IGeoCity, IGeoRegion } from '../../interfaces/geo.interface';
import { validateUserSQL } from '../../interfaces/user/joi/user.interface.joi';
import { IUserUpdateRequest } from '../../interfaces/user/seller.interface';
import { authActions } from "../../redux/slices";
import { sellerActions } from "../../redux/slices/seller.slice";
import { FindCarById } from '../../forms/car/FindCarById';

const ProfilePage: FC = () => {

    const dispatch = useAppDispatch();
    const { reset, handleSubmit, register } = useForm<IUserUpdateRequest>();

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

    const { carErrors } = useAppSelector(state => state.carReducer);

    const [getResponse, setResponse] = useState('');
    const { regions, cities } = useAppSelector(state => state.sellerReducer);

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
        console.log(JSON.stringify(userToUpdate) + "update car")

        const updatedUser: Partial<IUserUpdateRequest> = {};

        // Iterate over the keys in car object and add non-empty values to updatedCar
        Object.keys(userToUpdate).forEach(key => {
            const value = userToUpdate[key as keyof IUserUpdateRequest];
            if (value !== undefined && value !== null && value !== '') {
                updatedUser[key as keyof IUserUpdateRequest] = value;
            }
        });

        console.log(getCarRegion + "get car region")
        console.log(JSON.stringify(updatedUser) + "updatedCar json");

        await dispatch(sellerActions.updateById({ id: user.id, body: updatedUser }))
            .then((res) => {
                const type = res.type;
                const lastWord = type.substring(type.lastIndexOf("/") + 1);

                console.log(JSON.stringify(res.payload) + "result payload");

                if (lastWord === "fulfilled") {
                    setResponse("Car updated successfully");
                    setCarCity('');
                    setCarRegion('');
                    reset();
                }
            });
    };



    useEffect(() => {
        dispatch(sellerActions.getByToken());
    }, []);

    let userComponent;

    if (user === null) {
        userComponent = <div>Loading...
            <button onClick={() => dispatch(authActions.refresh())}>refresh</button></div>;
    } else if (user.role == ERole.USER && validateUserSQL(user)) {
        userComponent = <SellerProfile seller={user} />;
    } else if (user.role == ERole.ADMIN && validateUserSQL(user)) {
        userComponent = <AdminProfile seller={user} />;
    } else if (user.role == ERole.MANAGER && validateUserSQL(user)) {
        userComponent = <ManagerProfile seller={user} />;
    } else {
        userComponent = <div>User type not recognized</div>;
    }

    return (
        <div>
            <div>Profile</div>
            {userComponent}
            <hr />
            <ChangePasswordForm />
            <hr />
            <div>Change account info</div>
            <form onSubmit={handleSubmit(save)}>
                <div>
                    <div>{getResponse ? getResponse : <div>{carErrors?.message}</div>}</div>
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
                    <input autoComplete={"off"} type="text"
                        placeholder={'number'} {...register('number')} />
                </div>
                <div>
                    <input autoComplete={"off"} type="text"
                        placeholder={'name'} {...register('name')} />
                </div>
                <div>
                    <input autoComplete={"off"} type="text"
                        placeholder={'lastName'} {...register('lastName')} />
                </div>
                <button>update user info</button>
            </form>
            <hr />
            <FindCarById />
        </div>

    );
};

export { ProfilePage };

