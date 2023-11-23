import React, {FC} from 'react';
import {RegisterSellerForm} from "../forms/auth/register/RegisterSellerForm";
import {RegisterCustomerForm} from "../forms/auth/register/RegisterCustomerForm";
import {GenerateManagerForm} from "../forms/auth/codes/GenerateManagerForm";
import {GenerateAdminForm} from "../forms/auth/codes/GenerateAdminForm";
import {RegisterManagerForm} from "../forms/auth/register/RegisterManagerForm";
import {RegisterAdminForm} from "../forms/auth/register/RegisterAdminForm";
import {ActivateForm} from "../forms/auth/activate/ActivateForm";

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
            <ActivateForm/>
            <br/>

            <hr/>
            <GenerateManagerForm/>
            <br/>
            <GenerateAdminForm/>

        </div>
    );
};

export {RegisterPage};