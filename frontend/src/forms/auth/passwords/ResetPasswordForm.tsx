import { FC, useState } from 'react';
import { SubmitHandler, useForm } from "react-hook-form";
import { useAppDispatch, useAppNavigate, useAppSelector } from "../../../hooks";
import { IChangePassword } from "../../../interfaces";
import { authActions } from "../../../redux/slices";

const ResetPasswordForm: FC = () => {
    const { handleSubmit, register } = useForm<IChangePassword>();
    const dispatch = useAppDispatch();
    const { resetPasswordErrors } = useAppSelector(state => state.authReducer);
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
            }))

            if (!resetPasswordErrors) {
                navigate("/profile");
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
