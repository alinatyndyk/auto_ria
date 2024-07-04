import { FC, useEffect, useState } from 'react';
import { SubmitHandler, useForm } from "react-hook-form";
import { useAppDispatch, useAppNavigate, useAppSelector } from "../../../hooks";
import { INewPassword } from "../../../interfaces";
import { authActions } from "../../../redux/slices";
import './PasswordForm.css'; // Importing CSS file for styling


const ChangePasswordForm: FC = () => {
    const { reset, handleSubmit, register } = useForm<INewPassword>();
    const dispatch = useAppDispatch();
    const { changePasswordErrors } = useAppSelector(state => state.authReducer);
    const navigate = useAppNavigate();

    const [getResponse, setResponse] = useState<String | null>(null);
    const [showResponse, setShowResponse] = useState<boolean>(false);

    useEffect(() => {
        if (getResponse != null) {
            setShowResponse(true);
            const timer = setTimeout(() => {
                setShowResponse(false);
                setResponse(null);
            }, 5000);

            return () => clearTimeout(timer);
        }
    }, [getResponse]);

    const activate: SubmitHandler<INewPassword> = async (newPassword: INewPassword) => {
        const { type } = await dispatch(authActions.changePassword(newPassword));
        const lastWord = type.substring(type.lastIndexOf("/") + 1);

        if (lastWord === "fulfilled") {
            setResponse("Password changed successfully");
        } else {
            setResponse(null);
        }

        reset();
    };

    return (
        <div className="password-form">
            <div className="form-header">
                Change Password
            </div>
            <div className="error-message">
                {changePasswordErrors && <div>{changePasswordErrors.message}</div>}
                {showResponse && <div>{getResponse}</div>}
            </div>
            <form onSubmit={handleSubmit(activate)} className="password">
                <div className="form-group">
                    <input
                        type="password"
                        placeholder="Enter new password"
                        {...register('newPassword', { required: true })}
                    />
                </div>
                <button type="submit" className="form-button">Change Password</button>
            </form>
        </div>
    );
};

export { ChangePasswordForm };

