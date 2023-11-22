import React, {FC} from 'react';
import {LoginForm} from "../forms";
import {LogOutForm} from "../forms/auth/LogOutForm";
import {RegisterSellerForm} from "../forms/auth/register/RegisterSellerForm";
import {RegisterCustomerForm} from "../forms/auth/register/RegisterCustomerForm";
import {ActivateSellerForm} from "../forms/auth/activate/ActivateSellerForm";
import {ActivateCustomerForm} from "../forms/auth/activate/ActivateCustomerForm";
import {GenerateManagerForm} from "../forms/auth/codes/GenerateManagerForm";
import {GenerateAdminForm} from "../forms/auth/codes/GenerateAdminForm";
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


            <hr/>
            <ActivateSellerForm/>
            <br/>
            <ActivateCustomerForm/>


            <hr/>
            <GenerateManagerForm/>
            <br/>
            <GenerateAdminForm/>

        </div>
    );
};

export {RegisterPage};