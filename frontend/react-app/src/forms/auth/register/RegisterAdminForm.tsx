import React, {FC, useState} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {useAppDispatch, useAppNavigate, useAppSelector} from "../../../hooks";
import {authActions} from "../../../redux/slices";
import {IAdminInput} from "../../../interfaces/user/admin.interface";

const RegisterAdminForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<IAdminInput>();
    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();
    const {errors} = useAppSelector(state => state.authReducer);

    const [getResponse, setResponse] = useState('');

    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');

    const registerSeller: SubmitHandler<IAdminInput> = async (customer: IAdminInput) => {

        if (code) {
        const {payload} = await dispatch(authActions.registerAdmin({adminInput: customer, code: code ?? ""}));
            setResponse(String(payload));

            if(!errors) {
                // navigate('/profile');
            }

        } else {
            setResponse("No code provided in url");
        }


        // reset();
    }
    return (
        <div>
            Register an admin
            {errors ? <div>{errors?.message}</div> : <div>{getResponse}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(registerSeller)}>
                <div>
                    <input type="text" placeholder={'name'} {...register('name')}/>
                </div>
                <div>
                    <input type="text" placeholder={'last name'} {...register('lastName')}/>
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

export {RegisterAdminForm};