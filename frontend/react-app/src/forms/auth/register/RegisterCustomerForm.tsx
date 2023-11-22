import React, {FC, useState} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {useAppDispatch, useAppSelector} from "../../../hooks";
import {authActions} from "../../../redux/slices";
import {ICustomerInput} from "../../../interfaces/customer.interface";

const RegisterCustomerForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<ICustomerInput>();
    const dispatch = useAppDispatch();
    const {errors} = useAppSelector(state => state.authReducer);

    const [getResponse, setResponse] = useState('');

    const registerSeller: SubmitHandler<ICustomerInput> = async (customer: ICustomerInput) => {

        const {payload} = await dispatch(authActions.registerCustomer(customer));

        setResponse(String(payload));

        // reset();
    }
    return (
        <div>
            Register a customer
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

export {RegisterCustomerForm};