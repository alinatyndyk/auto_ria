import React, {FC, useState} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {useAppDispatch, useAppNavigate, useAppSelector} from "../../../hooks";
import {authActions} from "../../../redux/slices";
import {INewPassword} from "../../../interfaces";

const ChangePasswordForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<INewPassword>();
    const dispatch = useAppDispatch();
    const {errors} = useAppSelector(state => state.authReducer);
    const navigate = useAppNavigate();

    const [getResponse, setResponse] = useState('');

    const activate: SubmitHandler<INewPassword> = async (newPassword: INewPassword) => {

        const {payload} = await dispatch(authActions.changePassword(newPassword));

        setResponse(String(payload));

        reset();
    }
    return (
        <div>
            <div>
                <button onClick={() => navigate('/cars')}>Cars</button>
            </div>
            Activate seller
            {errors ? <div>{errors?.message}</div> : <div>{getResponse}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(activate)}>
                <div>
                    <input type="text" placeholder={'new password'} {...register('newPassword')}/>
                </div>
                <button>Register</button>
            </form>
        </div>
    );
};

export {ChangePasswordForm};