import React, {FC, useState} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {useAppDispatch, useAppSelector} from "../../../hooks";
import {authActions} from "../../../redux/slices";
import {IActivationCode} from "../../../interfaces";

const ActivateCustomerForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<IActivationCode>();
    const dispatch = useAppDispatch();
    const {errors} = useAppSelector(state => state.authReducer);

    //error for everyone

    const [getResponse, setResponse] = useState('');

    const activate: SubmitHandler<IActivationCode> = async (code: IActivationCode) => {

        const {payload} = await dispatch(authActions.activateCustomer(code));

        setResponse(String(payload));

        // reset();
    }
    return (
        <div>
            Activate customer
            {errors ? <div>{errors?.message}</div> : <div>{getResponse}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(activate)}>
                <div>
                    <input type="text" placeholder={'code'} {...register('code')}/>
                </div>
                <button>Register</button>
            </form>
        </div>
    );
};

export {ActivateCustomerForm};