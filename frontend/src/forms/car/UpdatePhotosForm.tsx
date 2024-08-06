import { FC, useEffect, useState } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { useAppDispatch, useAppNavigate, useAppSelector } from '../../hooks';
import { carActions } from '../../redux/slices';

import './FindCarById.css';


const UpdatePhotosForm: FC = () => {
    // const { reset, handleSubmit, register } = useForm();
    // const navigate = useAppNavigate();
    // const dispatch = useAppDispatch();
    // const { errorGetById } = useAppSelector(state => state.carReducer);

    // const find: SubmitHandler<> = async (body) => {
    //     try {

    //         let photos = [];
    //     for (let i = 0; i < body.pictures.length; i++) {
    //         photos.push(body.pictures[i]);
    //     }

    //         await dispatch(carActions.updatePictures(body.pictures)).unwrap();
    //         navigate(`/cars/${body.id}`);
    //     } catch (err) {
    //         reset();
    //     }
    // };


    return (
        <div>
            {/* <div>Update</div>
            {errorGetById && <div className="errorMessage">{errorGetById?.message}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(find)}>
            <div>
                    <input
                        formEncType="multipart/form-data"
                        type="file"
                        multiple={true}
                        placeholder={'Pictures'}
                        {...register('pictures')}
                    />
                </div>
                <button type="submit">Go to car</button>
            </form> */}
        </div>
    );
};

export { UpdatePhotosForm };


