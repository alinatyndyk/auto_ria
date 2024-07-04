import React, { FC } from 'react';
import { RegisterSellerForm } from "../../forms/auth/register/RegisterSellerForm";
import { useParams } from "react-router";
import ErrorForbidden from '../error/ErrorForbidden';

const RegisterPage: FC = () => {

    const { role } = useParams<{ role: string }>();

    let userComponent;

    if (role === "USER") {
        userComponent = <RegisterSellerForm/>
    } else {
        userComponent = <ErrorForbidden cause='User type wasn*t recognized'/>
    }

    return (
        <div>
            {userComponent}
        </div>
    );
};

export { RegisterPage };