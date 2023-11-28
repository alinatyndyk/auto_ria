import React, {FC} from 'react';
import {RegisterSellerForm} from "../forms/auth/register/RegisterSellerForm";
import {RegisterCustomerForm} from "../forms/auth/register/RegisterCustomerForm";
import {RegisterManagerForm} from "../forms/auth/register/RegisterManagerForm";
import {RegisterAdminForm} from "../forms/auth/register/RegisterAdminForm";

const RegisterPage: FC = () => {
    return (
        <div>
            <RegisterSellerForm/>
            <br/>
            <RegisterCustomerForm/>
            <br/>
            <RegisterManagerForm/>
            <br/>
            <RegisterAdminForm/>

        </div>
    );
};

export {RegisterPage};