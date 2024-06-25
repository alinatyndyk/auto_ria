import { FC, useEffect, useState } from 'react';
import { SubmitHandler, useForm } from "react-hook-form";
import { useAppDispatch, useAppNavigate, useAppSelector } from "../../../hooks";
import { INewPassword } from "../../../interfaces";
import { authActions } from "../../../redux/slices";

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
    }
    return (
        <div>
            <div>
                <button onClick={() => navigate('/cars')}>Cars</button>
            </div>
            <div>
                Change password
            </div>
            {/* {changePasswordErrors ? <div>{changePasswordErrors?.message}</div> : <div>{getResponse}</div>} */}
            <div>
                {changePasswordErrors ? (
                    <div>{changePasswordErrors?.message}</div>
                ) : (
                    showResponse && <div>{getResponse}</div>
                )}
            </div>
            <form encType="multipart/form-data" onSubmit={handleSubmit(activate)}>
                <div>
                    <input type="text" placeholder={'new password'} {...register('newPassword')} />
                </div>
                <button>change password</button>
            </form>
        </div>
    );
};

export { ChangePasswordForm };
