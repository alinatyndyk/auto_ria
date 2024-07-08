import { FC } from 'react';
import { SubmitHandler, useForm } from "react-hook-form";
import { useAppDispatch, useAppNavigate, useAppSelector } from "../../../hooks";
import { IChangePassword } from "../../../interfaces";
import { IUserResponse } from '../../../interfaces/user/seller.interface';
import ErrorForbidden from '../../../pages/error/ErrorForbidden';
import { authActions } from "../../../redux/slices";
import { sellerActions } from '../../../redux/slices/seller.slice';

const ResetPasswordForm: FC = () => {
    const { handleSubmit, register } = useForm<IChangePassword>();
    const dispatch = useAppDispatch();
    const { resetPasswordErrors } = useAppSelector(state => state.authReducer);
    const navigate = useAppNavigate();

    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');

    if (localStorage.getItem("isAuth") === "true") {
        return <ErrorForbidden cause='You are already logged in' />
    }

    const activate: SubmitHandler<IChangePassword> = async (newPassword: IChangePassword) => {

        if (!code) {
            return <ErrorForbidden cause='No code for activation' />
        } else {
            await dispatch(authActions.resetPassword({
                newPassword: newPassword.newPassword,
                code: code
            }))

            if (!resetPasswordErrors) {
                dispatch(sellerActions.getByToken()).then((res) => {
                    navigate('/profile', { state: { receiver: res.payload as IUserResponse } });
                })
            }
        }
    }
    return (
        <div>
            <div>
                <button onClick={() => navigate('/cars')}>Cars</button>
            </div>
            Activate seller
            {resetPasswordErrors ? <div>{resetPasswordErrors?.message}</div> : null}
            <form encType="multipart/form-data" onSubmit={handleSubmit(activate)}>
                <div>
                    <input type="text" placeholder={'new password'} {...register('newPassword')} />
                </div>
                <button style={{
                    alignItems: "center",
                    width: "100px",
                    height: "30px",
                    color: "white",
                    border: "none",
                    backgroundColor: "green",
                }}>reset</button>
            </form>
        </div>
    );
};

export { ResetPasswordForm };

