import React, { FC, useEffect, useState } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '../../hooks';
import { ICreateCar, ICreateInputCar, IError } from '../../interfaces';
import { EGeoState, IGeoCity, IGeoRegion } from '../../interfaces/geo.interface';
import { carActions } from '../../redux/slices';
import { sellerActions } from '../../redux/slices/seller.slice';
import styles from './CarForm.module.css';

export enum ECurrency {
    UAH = 'UAH',
    EUR = 'EUR',
    USD = 'USD',
}

const CarForm: FC = () => {
    const { reset, handleSubmit, register } = useForm<ICreateInputCar>();
    const { brands, models } = useAppSelector(state => state.carReducer);
    const { carCreateRegions: regions, carCreateCities: cities } = useAppSelector(state => state.sellerReducer);
    const dispatch = useAppDispatch();

    const [getResponse, setResponse] = useState('');
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

    useEffect(() => {
        dispatch(carActions.getAllBrands());
        setCurrencies(Object.values(ECurrency));
    }, [dispatch]);

    useEffect(() => {
        setModels(models);
    }, [models]);

    useEffect(() => {
        setBrands(brands);
    }, [brands]);

    useEffect(() => {
        setRegions(regions);
    }, [regions]);

    useEffect(() => {
        setCities(cities);
    }, [cities]);

    const save: SubmitHandler<ICreateInputCar> = async car => {
        let photos = [];
        for (let i = 0; i < car.pictures.length; i++) {
            photos.push(car.pictures[i]);
        }
        car.city = getCarCity;
        car.region = getCarRegion;
        car.brand = getBrand;
        car.model = getModel;
        const updatedCar: ICreateCar = {
            ...car,
            pictures: photos,
        };
        const { payload, type } = await dispatch(carActions.create(updatedCar));

        const lastWord = type.substring(type.lastIndexOf('/') + 1);
        if (lastWord === 'fulfilled') {
            setResponse('Car created successfully');
            setCarCity('');
            setCarRegion('');
            setBrand('');
            setModel('');
            reset();
        } else {
            const x = payload as IError;
            setResponse(String(x.message));
        }

    };

    const handleModels = async (brand: string) => {
        await dispatch(carActions.getAllModelsByBrand(brand));
    };

    const handleInputChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        if (!getRegionInput) setCarRegion(event.target.value);
        await dispatch(sellerActions.getRegionsByPrefix({ info: event.target.value, stateToFill: EGeoState.CAR_CREATE }));
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
        await dispatch(sellerActions.getRegionsPlaces({ info: getCarRegionId, stateToFill: EGeoState.CAR_CREATE }));
    };

    const handleCityClick = (cityName: string) => {
        setCarCity(cityName);
        setCityInput(true);
        setIsCityVisible(false);
    };

    return (
        <div className={styles.carForm}>
            <div style={{ fontSize: '18px', fontWeight: '600', marginBottom: '10px' }}>Create new car</div>
            {getResponse === "Car created successfully" ?
                <div style={{ color: 'green', fontSize: '14px' }}>{getResponse}</div>
                : <div style={{ color: 'darkred', fontSize: '14px' }}>{getResponse}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(save)}>
                <div className={styles.inputContainer}>
                    <input
                        className={styles.input}
                        type="text"
                        readOnly={true}
                        value={getBrand}
                        placeholder={'Brand'}
                        {...register('brand')}
                        onClick={() => {
                            setIsBrandsVisible(true);
                        }}
                    />
                </div>
                {isBrandsVisible && (
                    <div className={styles.dropdown}>
                        {getBrands.map(brand => (
                            <div
                                key={brand}
                                onClick={() => {
                                    setBrand(brand);
                                    setModel('');
                                    handleModels(brand);
                                    setIsBrandsVisible(false);
                                }}
                            >
                                {brand}
                            </div>
                        ))}
                    </div>
                )}
                <div className={styles.inputContainer}>
                    <input
                        className={styles.input}
                        autoComplete={'off'}
                        readOnly={true}
                        value={getModel}
                        type="text"
                        placeholder={'Model'}
                        {...register('model', { value: getModel })}
                        onClick={() => {
                            setIsModelsVisible(true);
                        }}
                    />
                </div>
                {isModelsVisible && (
                    <div className={styles.dropdown}>
                        {getModels.map(model => (
                            <div
                                key={model}
                                onClick={() => {
                                    setModel(model);
                                    setIsModelsVisible(false);
                                }}
                            >
                                {model}
                            </div>
                        ))}
                    </div>
                )}
                <div className={styles.inputContainer}>
                    <input
                        className={styles.input}
                        autoComplete={'off'}
                        type="number"
                        placeholder={'PowerH'}
                        {...register('powerH', {
                            pattern: /^[0-9]*$/,
                        })}
                    />
                </div>
                <div className={styles.inputContainer}>
                    <input
                        className={styles.input}
                        autoComplete={'off'}
                        placeholder={'Region'}
                        {...register('region', { value: getCarRegion })}
                        value={getCarRegion}
                        disabled={getRegionInput}
                        type="text"
                        onChange={handleInputChange}
                    />
                    <button
                        className={styles.button}
                        type="button"
                        onClick={() => {
                            setRegionInput(false);
                            setCarRegion('');
                            setIsRegionVisible(true);
                            setCarCity('');
                        }}
                    >
                        Change Region
                    </button>
                </div>
                {isRegionVisible && (
                    <div className={styles.dropdown}>
                        {getRegions.map(region => (
                            <div
                                key={region.isoCode}
                                onClick={() => {
                                    handleRegionClick(region);
                                    setResponse('');
                                }}
                            >
                                {region.name}
                            </div>
                        ))}
                    </div>
                )}
                <div className={styles.inputContainer}>
                    <input
                        className={styles.input}
                        autoComplete={'off'}
                        placeholder={'City'}
                        {...register('city', { value: getCarCity })}
                        value={getCarCity}
                        disabled={getCityInput}
                        type="text"
                        onChange={handleCityInputChange}
                    />
                    <button
                        className={styles.button}
                        type="button"
                        onClick={() => {
                            setCityInput(false);
                            setCarCity('');
                            setIsCityVisible(true);
                        }}
                    >
                        Change City
                    </button>
                </div>
                {isCityVisible && (
                    <div className={styles.dropdown}>
                        {isCityVisible &&
                            getCities.map(city => {
                                if (city.name.toLowerCase().startsWith(getCityInputValue.toLowerCase())) {
                                    return (
                                        <div
                                            key={city.name}
                                            onClick={() => {
                                                handleCityClick(city.name);
                                                setResponse('');
                                            }}
                                        >
                                            {city.name}
                                        </div>
                                    );
                                }
                            })}
                    </div>
                )}
                <div className={styles.inputContainer}>
                    <input className={styles.input} type="number" placeholder={'Price'} {...register('price')} />
                </div>
                <div className={styles.inputContainer}>
                    <input
                        className={styles.input}
                        autoComplete={'off'}
                        type="text"
                        readOnly={true}
                        value={getCurrency}
                        placeholder={'Currency'}
                        {...register('currency', { value: getCurrency })}
                        onClick={() => {
                            setIsCurrencyVisible(true);
                        }}
                    />
                </div>
                {isCurrencyVisible && (
                    <div className={styles.dropdown}>
                        {getCurrencies.map(curr => (
                            <div
                                key={curr}
                                onClick={() => {
                                    setCurrency(curr);
                                    setIsCurrencyVisible(false);
                                }}
                            >
                                {curr}
                            </div>
                        ))}
                    </div>
                )}
                <div className={styles.inputContainer}>
                    <input
                        className={styles.input}
                        formEncType="multipart/form-data"
                        type="file"
                        multiple={true}
                        placeholder={'Pictures'}
                        {...register('pictures')}
                    />
                </div>
                <div className={styles.inputContainer}>
                    <input
                        className={styles.input}
                        autoComplete={'off'}
                        type="text"
                        placeholder={'Description'}
                        {...register('description')}
                    />
                </div>
                <button className={`${styles.button} ${styles.submitButton}`} type="submit">Save</button>
            </form>
        </div>
    );
};

export { CarForm };