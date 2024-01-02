import React, {FC, useState} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {useAppDispatch, useAppNavigate, useAppSelector} from "../../../hooks";
import {authActions} from "../../../redux/slices";
import {IChangePassword} from "../../../interfaces";

const ResetPasswordForm: FC = () => {
    const {handleSubmit, register} = useForm<IChangePassword>();
    const dispatch = useAppDispatch();
    const {errors} = useAppSelector(state => state.authReducer);
    const navigate = useAppNavigate();

    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');

    const [getResponse, setResponse] = useState('');

    const activate: SubmitHandler<IChangePassword> = async (newPassword: IChangePassword) => {

        if (!code) {
            setResponse("Error. Register code not provided");
        } else {
            await dispatch(authActions.resetPassword({
                newPassword: newPassword.newPassword,
                code: code
            })).catch((error) => {
                setResponse(error.toString);
            });
        }
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
                <button>reset</button>
            </form>
        </div>
    );
};

export {ResetPasswordForm};