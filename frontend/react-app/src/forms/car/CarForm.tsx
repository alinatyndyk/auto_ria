import React, {FC, useEffect, useState} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {ICreateCar, ICreateInputCar} from "../../interfaces";
import {useAppDispatch, useAppSelector} from "../../hooks";
import {carActions} from "../../redux/slices";
import {sellerActions} from "../../redux/slices/seller.slice";
import {IGeoCity, IGeoRegion} from "../../interfaces/geo.interface";

enum ECurrency {
    UAH = "UAH", EUR = "EUR", USD = "USD"
}

const CarForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<ICreateInputCar>();
    const {errors, brands, models} = useAppSelector(state => state.carReducer);
    const [getResponse, setResponse] = useState('');

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

    const [getBrands, setBrands] = useState<string[]>([]);
    const [isBrandsVisible, setIsBrandsVisible] = useState(false);

    const [getModels, setModels] = useState<string[]>([]);
    const [isModelsVisible, setIsModelsVisible] = useState(false);

    const [getBrand, setBrand] = useState('');
    const [getModel, setModel] = useState('');

    const [isCurrencyVisible, setIsCurrencyVisible] = useState(false);
    const [getCurrency, setCurrency] = useState<ECurrency>(ECurrency.EUR);
    const [getCurrencies, setCurrencies] = useState<ECurrency[]>([]);

    const dispatch = useAppDispatch();
    const save: SubmitHandler<ICreateInputCar> = async (car: ICreateInputCar) => {
        console.log(car, "car")
        let photos = [];

        for (let i = 0; i < car.pictures.length; i++) {
            photos.push(car.pictures[i])
        }

        car.city = getCarCity;
        car.region = getCarRegion;
        car.brand = getBrand;
        car.model = getModel;

        const updatedCar: ICreateCar = {
            ...car,
            pictures: photos
        };

        const {payload} = await dispatch(carActions.create(updatedCar));
        if (!errors) {
            setResponse("Car created successfully");
        }

        // reset();
    }
    useEffect(() => {
        setCurrencies(Object.values(ECurrency));
        handleBrands();
    }, [])

    const handleBrands = async () => {
        await dispatch(carActions.getAllBrands());
    };

    const handleModels = async (brand: string) => {
        await dispatch(carActions.getAllModelsByBrand(brand));
    };

    useEffect(() => {
        setModels(models);
    }, [models])

    useEffect(() => {
        setBrands(brands);
    }, [brands])

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
            {errors ? <div>{errors?.message}</div> : <div>{getResponse}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(save)}>
                <div>
                    <input type="text" readOnly={true} value={getBrand} placeholder={'brand'}
                           {...register('brand')}
                           onClick={() => {
                               setIsBrandsVisible(true);
                           }}/>
                </div>
                {
                    isBrandsVisible &&
                    <div>
                        {getBrands.map((brand) => (
                            <div key={brand} onClick={() => {
                                setBrand(brand);
                                handleModels(brand);
                                setIsBrandsVisible(false);
                            }}>
                                {brand}
                            </div>
                        ))}
                    </div>
                }
                <div>
                    <input autoComplete={"off"} readOnly={true} value={getModel} type="text"
                           placeholder={'model'} {...register('model', {value: getModel})}
                           onClick={() => {
                               setIsModelsVisible(true);
                           }}/>
                </div>
                {isModelsVisible &&
                    <div>
                        {getModels.map((model) => (
                            <div key={model} onClick={() => {
                                setModel(model);
                                setIsModelsVisible(false);
                            }}>
                                {model}
                            </div>
                        ))}
                    </div>
                }
                <div>
                    <input autoComplete={"off"} type="text" placeholder={'powerH'} {...register('powerH')}/>
                </div>
                <div>
                    <input placeholder={'region'} {...register('region', {value: getCarRegion})}
                           value={getCarRegion} disabled={getRegionInput}
                           autoComplete={"off"} type="text" onChange={handleInputChange}/>
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
                    <input placeholder={'city'} {...register('city', {value: getCarCity})}
                           value={getCarCity} disabled={getCityInput}
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
                    <input type="number" placeholder={'price'} {...register('price')}/>
                </div>
                <div>
                    <input autoComplete={"off"} type="text" readOnly={true} value={getCurrency}
                           placeholder={'currency'} {...register('currency', {value: getCurrency})}
                           onClick={() => {
                               setIsCurrencyVisible(true);
                           }}/>
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
                    <input formEncType="multipart/form-data" type="file" multiple={true}
                           placeholder={'pictures'} {...register('pictures')}/>
                </div>
                <div>
                    <input autoComplete={"off"} type="text" placeholder={'description'} {...register('description')}/>
                </div>
                <button>save</button>
            </form>
        </div>
    );
};

export {CarForm};