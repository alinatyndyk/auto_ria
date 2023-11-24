import React, {FC} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {IAuthRequest} from "../../interfaces";
import {useAppDispatch, useAppSelector} from "../../hooks";
import {authActions} from "../../redux/slices";
import {useNavigate} from "react-router";
import {auth} from "../../constants";
import {sellerActions} from "../../redux/slices/seller.slice";
import {authService} from "../../services";

const LoginForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<IAuthRequest>();
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const {errors, authId, isAuth} = useAppSelector(state => state.authReducer);

    const login: SubmitHandler<IAuthRequest> = async (info: IAuthRequest) => {

        await dispatch(authActions.login(info));

        navigate('/profile')

    }

    return (
        <div>
            <div>Log in here</div>
            <form onSubmit={handleSubmit(login)}>
                <input placeholder={"email"} {...register('email')}/>
                <input placeholder={"password"} {...register('password')}/>
                <button>login</button>
            </form>
            {errors ? <div>{errors?.message}</div> : <div></div>}
        </div>
    );
};

export {LoginForm};