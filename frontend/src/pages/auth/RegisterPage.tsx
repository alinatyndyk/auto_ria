import React, { FC } from 'react';
import { RegisterSellerForm } from "../../forms/auth/register/RegisterSellerForm";
import { useParams } from "react-router";

const RegisterPage: FC = () => {

    const { role } = useParams<{ role: string }>();

    let userComponent;

    if (role === "USER") {
        userComponent = <RegisterSellerForm />
    } else {
        userComponent = <div>User type not recognized</div>;
    }

    return (
        <div>
            {userComponent}
        </div>
    );
};

export { RegisterPage };