import { FC, useEffect, useState } from 'react';
import { useLocation } from 'react-router';
import LoadingPage from '../../components/LoadingPage';
import { AdminProfile } from "../../components/profiles/AdminProfile";
import { ManagerProfile } from "../../components/profiles/ManagerProfile";
import { SellerProfile } from "../../components/profiles/SellerProfile";
import { ERole } from "../../constants/role.enum";
import { UpdateUserForm } from '../../forms/auth/logs/update/UpdateUserForm';
import { ChangePasswordForm } from '../../forms/auth/passwords/ChangePasswordForm';
import { useAppSelector } from "../../hooks";
import { validateUserSQL } from '../../interfaces/user/joi/user.interface.joi';
import { IUserResponse } from '../../interfaces/user/seller.interface';
import { ChatsPage } from '../WebSocketComponents';
import ErrorForbidden from '../error/ErrorForbidden';
import { securityService } from '../../services/security.service';


const ProfilePage: FC = () => {

    const location = useLocation();

    const { user, isUserLoading, errorGetById } = useAppSelector(state => state.sellerReducer);
    const receiver = location.state?.user as IUserResponse | undefined;

    const [currUser, setCurrUser] = useState<IUserResponse>();

    useEffect(() => {

        if (user === null && receiver !== undefined) {
            setCurrUser(receiver);
        } else if (user !== null) {
            setCurrUser(user);
        }

        if (currUser !== null && currUser !== undefined) {
            const obj = securityService.encryptObject(currUser);
            localStorage.setItem("authorization", obj);
        }
    }, [user]);

    if (user === null && receiver === undefined) {
        return <ErrorForbidden cause='Could not access profile. Please log in' />;
    }

    if (isUserLoading) {
        return <LoadingPage />;
    }

    if (errorGetById) {
        return <ErrorForbidden cause='The account could not be found' />;
    }

    let userComponent;

    if (currUser?.role === ERole.USER && validateUserSQL(currUser)) {
        userComponent = <SellerProfile seller={currUser} />;
    } else if (currUser?.role === ERole.ADMIN && validateUserSQL(currUser)) {
        userComponent = <AdminProfile seller={currUser} />;
    } else if (currUser?.role === ERole.MANAGER && validateUserSQL(currUser)) {
        userComponent = <ManagerProfile seller={currUser} />;
    } else {
        userComponent = <div>User type not recognized</div>;
    }

    return (
        <div>
            {userComponent}
            <div style={{ display: "flex", alignItems: "center", flexDirection: "column" }}>
                <h2>Change your account information</h2>
                <div style={{ margin: "20px", display: "flex", columnGap: "20px" }}>
                    <UpdateUserForm />
                    <br />
                    <ChangePasswordForm />
                </div>
            </div>
            <div>
                <div>Your chats</div>
                <ChatsPage />
            </div>
        </div>

    );
};

export { ProfilePage };