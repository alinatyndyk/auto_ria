import React, {FC} from 'react';
import {RegisterSellerForm} from "../forms/auth/register/RegisterSellerForm";
import {RegisterCustomerForm} from "../forms/auth/register/RegisterCustomerForm";
import {RegisterManagerForm} from "../forms/auth/register/RegisterManagerForm";
import {RegisterAdminForm} from "../forms/auth/register/RegisterAdminForm";
import {useParams} from "react-router";

const RegisterPage: FC = () => {

    const {role} = useParams<{ role: string }>();

    let userComponent;

    if (role == "SELLER") {
        userComponent = <RegisterSellerForm/>
    } else if (role == "CUSTOMER") {
        userComponent = <RegisterCustomerForm/>
    } else if (role == "ADMIN") {
        userComponent = <RegisterAdminForm/>
    } else if (role == "MANAGER") {
        userComponent = <RegisterManagerForm/>
    } else {
        userComponent = <div>User type not recognized</div>;
    }

    return (
        <div>
            {userComponent}
        </div>
    );
};

export {RegisterPage};