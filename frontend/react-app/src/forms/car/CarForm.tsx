import React, {FC, useState} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {ICreateCar, ICreateInputCar} from "../../interfaces";
import {useAppDispatch, useAppSelector} from "../../hooks";
import {carActions} from "../../redux/slices";

const CarForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<ICreateInputCar>();
    const {errors} = useAppSelector(state => state.carReducer);
    const [getResponse, setResponse] = useState('');


    const dispatch = useAppDispatch();
    const save: SubmitHandler<ICreateInputCar> = async (car: ICreateInputCar) => {
        let photos = [];

        for (let i = 0; i < car.pictures.length; i++) {
            photos.push(car.pictures[i])
        }
        const updatedCar: ICreateCar = {
            ...car,
            pictures: photos
        };

        const {payload} = await dispatch(carActions.create(updatedCar))
        setResponse(String(payload));

        // reset();
    }
    return (
        <div>
            car form
            {errors ? <div>{errors?.message}</div> : <div>{getResponse}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(save)}>
                <div>
                    <input type="text" placeholder={'brand'} {...register('brand')}/>
                </div>
                <div>
                    <input type="text" placeholder={'powerH'} {...register('powerH')}/>
                </div>
                <div>
                    <input type="text" placeholder={'city'} {...register('city')}/>
                </div>
                <div>
                    <input type="text" placeholder={'region'} {...register('region')}/>
                </div>
                <div>
                    <input type="text" placeholder={'price'} {...register('price')}/>
                </div>
                <div>
                    <input type="text" placeholder={'currency'} {...register('currency')}/>
                </div>
                <div>
                    <input type="text" placeholder={'model'} {...register('model')}/>
                </div>
                <div>
                    <input formEncType="multipart/form-data" type="file" multiple={true}
                           placeholder={'pictures'} {...register('pictures')}/>
                </div>
                <div>
                    <input type="text" placeholder={'description'} {...register('description')}/>
                </div>
                <button>save</button>
            </form>
        </div>
    );
}; //todo change pic ohne []

export {CarForm};