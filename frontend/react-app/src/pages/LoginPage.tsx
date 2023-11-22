import React, {FC} from 'react';
import {LoginForm} from "../forms";
import {LogOutForm} from "../forms/auth/LogOutForm";

const LoginPage: FC = () => {
    return (
        <div>
            <LoginForm/>
            <LogOutForm/>
        </div>
    );
};

export {LoginPage};