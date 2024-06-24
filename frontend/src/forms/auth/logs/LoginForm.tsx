import { FC } from 'react';
import { SubmitHandler, useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { Link } from "react-router-dom";
import { useAppDispatch, useAppSelector } from "../../../hooks";
import { IAuthRequest } from "../../../interfaces";
import { authActions } from "../../../redux/slices";

const LoginForm: FC = () => {
    const { reset, handleSubmit, register } = useForm<IAuthRequest>();
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const { loginErrors } = useAppSelector(state => state.authReducer);

    const login: SubmitHandler<IAuthRequest> = async (info: IAuthRequest) => {

        await dispatch(authActions.login(info)).then((res) => {
            const type = res.type;
            const lastWord = type.substring(type.lastIndexOf("/") + 1);

            if (lastWord != "fulfilled") {
                reset();
            } else {
                navigate('/profile');
            }
        });
    }

    return (
        <div style={{
            alignItems: "center",
            width: "400px",
            backgroundColor: "whitesmoke",
            display: "flex",
            flexDirection: "column"
        }}>
            <div>Log in here</div>
            <form onSubmit={handleSubmit(login)}>
                <div>
                    <input placeholder={"email"} {...register('email')} />
                </div>
                <div>
                    <input placeholder={"password"} {...register('password')} />
                </div>
                <button style={{
                    color: "white",
                    height: "25px",
                    width: "100px",
                    backgroundColor: "green",
                    border: "none"
                }}>login
                </button>
            </form>
            <Link to={'/auth/forgot-password'}>Forgot password?</Link>
            {loginErrors ? <div>{loginErrors?.message}</div> : null}
        </div>
    );
};

export { LoginForm };
