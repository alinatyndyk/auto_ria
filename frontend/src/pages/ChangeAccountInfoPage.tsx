import { FC } from 'react';
import { UpdateUserForm } from '../forms/auth/logs/update/UpdateUserForm';
import { ChangePasswordForm } from '../forms/auth/passwords/ChangePasswordForm';

const ChangeAccountInfoPage: FC = () => {

    return (
        <div style={{ display: "flex", alignItems: "center", flexDirection: "column" }}>
            <h2>Change your account information</h2>
            <div style={{ margin: "20px", display: "flex", columnGap: "20px" }}>
                <UpdateUserForm />
                <br />
                <ChangePasswordForm />
            </div>
        </div>
    );
};

export { ChangeAccountInfoPage };
