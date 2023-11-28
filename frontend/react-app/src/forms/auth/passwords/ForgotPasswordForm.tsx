import React, {FC, useState} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {useAppDispatch, useAppNavigate, useAppSelector} from "../../../hooks";
import {authActions} from "../../../redux/slices";
import {IForgotPassword} from "../../../interfaces";

const ForgotPasswordForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<IForgotPassword>();
    const dispatch = useAppDispatch();
    const {errors} = useAppSelector(state => state.authReducer);
    const navigate = useAppNavigate();

    const [getResponse, setResponse] = useState('');

    const activate: SubmitHandler<IForgotPassword> = async (email: IForgotPassword) => {

        const {payload} = await dispatch(authActions.forgotPassword(email));

        setResponse(String(payload));

        if (!errors) {
            reset(); //todo with tokens or not. if with navigate
        }

    }
    return (
        <div style={{
            alignItems: "center",
            width: "400px",
            backgroundColor: "whitesmoke",
            display: "flex",
            flexDirection: "column"
        }}>
            Restore your password here
            {errors ? <div>{errors?.message}</div> : <div>{getResponse}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(activate)}>
                <div>
                    <input type="text" placeholder={'email'} {...register('email')}/>
                </div>
                <div>

                    <button style={{
                        color: "white",
                        height: "25px",
                        width: "100px",
                        backgroundColor: "green",
                        border: "none"
                    }}>Send email
                    </button>
                </div>
            </form>
        </div>
    );
};

export {ForgotPasswordForm};