import { FC, useState } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '../../../hooks';
import { IForgotPassword } from '../../../interfaces';
import ErrorForbidden from '../../../pages/error/ErrorForbidden';
import { authActions } from '../../../redux/slices';
import '../logs/LoginForm.css';


const ForgotPasswordForm: FC = () => {
    const { handleSubmit, register } = useForm<IForgotPassword>();
    const dispatch = useAppDispatch();
    const { forgotPasswordErrors } = useAppSelector(state => state.authReducer);

    const [getResponse, setResponse] = useState('');

    const activate: SubmitHandler<IForgotPassword> = async (email: IForgotPassword) => {
        const { payload } = await dispatch(authActions.forgotPassword(email));
        setResponse(String(payload));
    };

    if (localStorage.getItem("isAuth") === "true") {
        return <ErrorForbidden cause='You are already logged in' />
    }

    return (
        <div style={{ display: 'flex', justifyContent: 'center', margin: '20px' }}>
            <div className="login-form">
                <div className="form-header">Restore your password here</div>
                {forgotPasswordErrors ? (
                    <div className="error-message">{forgotPasswordErrors?.message}</div>
                ) : (
                    <div className="error-message">{getResponse}</div>
                )}
                <form encType="multipart/form-data" onSubmit={handleSubmit(activate)} className="form-container">
                    <div className="form-group">
                        <input
                            type="text"
                            placeholder="Email"
                            {...register('email')}
                            className="form-input"
                        />
                    </div>
                    <div className="form-group">
                        <button type="submit" className="submit-button">
                            Send email
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export { ForgotPasswordForm };

