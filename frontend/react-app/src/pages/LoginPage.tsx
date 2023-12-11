import React, {FC} from 'react';
import {LoginForm} from "../forms";
import {LogOutForm} from "../forms/auth/LogOutForm";
import {ForgotPasswordForm} from "../forms/auth/passwords/ForgotPasswordForm";

const LoginPage: FC = () => {
    return (
        <div>
            <LoginForm/>
            {localStorage.getItem("isAuth") === "true" && <LogOutForm/>}
        </div>
    );
};

export {LoginPage};