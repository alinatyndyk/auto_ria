import React, {FC, useState} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {useAppDispatch, useAppSelector} from "../../../hooks";
import {ISellerInput} from "../../../interfaces/seller.interface";
import {authActions} from "../../../redux/slices";

const RegisterSellerForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<ISellerInput>();
    const dispatch = useAppDispatch();
    const {errors} = useAppSelector(state => state.authReducer);

    const [getResponse, setResponse] = useState('');

    const registerSeller: SubmitHandler<ISellerInput> = async (seller: ISellerInput) => {

        console.log(seller, "seller");

        const {payload} = await dispatch(authActions.registerSeller(seller));
        console.log(payload, "payload");

        setResponse(prevState => String(payload));

        // reset();
    }
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
                    <input type="text" placeholder={'city'} {...register('city')}/>
                </div>
                <div>
                    <input type="text" placeholder={'region'} {...register('region')}/>
                </div>
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