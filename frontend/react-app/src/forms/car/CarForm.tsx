import React, {FC} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {ICar, ICreateCar, ICreateInputCar} from "../../interfaces";
import {useAppDispatch} from "../../hooks";
import {carActions} from "../../redux/slices";

const CarForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<ICreateInputCar>();
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

        await dispatch(carActions.create(updatedCar))

        // reset();
    }
    return (
        <div>
            car form

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