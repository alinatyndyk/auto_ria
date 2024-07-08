import { FC } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { useNavigate } from 'react-router';
import { Link } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../../hooks';
import { IAuthRequest } from '../../../interfaces';
import { authActions } from '../../../redux/slices';
import './LoginForm.css'; // Import your CSS file for styling if needed
import { sellerActions } from '../../../redux/slices/seller.slice';
import { IUserResponse } from '../../../interfaces/user/seller.interface';

const LoginForm: FC = () => {
    const { reset, handleSubmit, register } = useForm<IAuthRequest>();
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const { loginErrors } = useAppSelector(state => state.authReducer);

    const login: SubmitHandler<IAuthRequest> = async (info: IAuthRequest) => {
        await dispatch(authActions.login(info)).then((res) => {
            const type = res.type;
            const lastWord = type.substring(type.lastIndexOf('/') + 1);

            if (lastWord !== 'fulfilled') {
                reset();
            } else if (lastWord === "fulfilled" && localStorage.getItem("access_token") !== null) {
                dispatch(sellerActions.getByToken()).then((res) => {
                    navigate('/profile', { state: { receiver: res.payload as IUserResponse } });
                })
            }
        });
    };

    return (
        <div className="login-form">
            <div className="form-header">Log in here</div>
            <form onSubmit={handleSubmit(login)} className="form-container">
                <div className="form-group">
                    <input placeholder="Email" {...register('email')} className="form-input" />
                </div>
                <div className="form-group">
                    <input placeholder="Password" {...register('password')} type="password" className="form-input" />
                </div>
                <button type="submit" className="submit-button">Login</button>
            </form>
            <Link to="/auth/forgot-password" className="forgot-password-link">Forgot password?</Link>
            {loginErrors && <div className="error-message">{loginErrors.message}</div>}
        </div>
    );
};

export default LoginForm;
