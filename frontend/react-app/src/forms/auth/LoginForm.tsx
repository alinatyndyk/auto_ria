import React, {FC} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {IAuthRequest} from "../../interfaces";
import {useAppDispatch} from "../../hooks";
import {authActions} from "../../redux/slices";

const LoginForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<IAuthRequest>();
    const dispatch = useAppDispatch();

    const login: SubmitHandler<IAuthRequest> = async (info: IAuthRequest) => {

        await dispatch(authActions.login(info));
        // reset();
    }

    return (
        <div>
            <div>Log in here</div>
            <form onSubmit={handleSubmit(login)}>
                <input placeholder={"email"} {...register('email')}/>
                <input placeholder={"password"} {...register('password')}/>
                <button>login</button>
            </form>
        </div>
    );
};

export {LoginForm};