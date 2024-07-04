import { FC, useEffect } from 'react';
import LoadingPage from '../../components/LoadingPage';
import { AdminProfile } from "../../components/profiles/AdminProfile";
import { ManagerProfile } from "../../components/profiles/ManagerProfile";
import { SellerProfile } from "../../components/profiles/SellerProfile";
import { ERole } from "../../constants/role.enum";
import { useAppDispatch, useAppSelector } from "../../hooks";
import { validateUserSQL } from '../../interfaces/user/joi/user.interface.joi';
import { sellerActions } from "../../redux/slices/seller.slice";
import { securityService } from '../../services/security.service';
import ErrorForbidden from '../error/ErrorForbidden';
import { ChangePasswordForm } from '../../forms/auth/passwords/ChangePasswordForm';
import { UpdateUserForm } from '../../forms/auth/logs/update/UpdateUserForm';


const ProfilePage: FC = () => {

    const dispatch = useAppDispatch();

    const { user, isUserLoading, errorGetById } = useAppSelector(state => state.sellerReducer);

    useEffect(() => {
            dispatch(sellerActions.getByToken());
        if (user) {
            const obj = securityService.encryptObject(user)
            localStorage.setItem("authorization", obj);
        }
    }, []);

    if (isUserLoading) {
        return <LoadingPage />
    }
    if (errorGetById) {
        return <ErrorForbidden cause='The account couldnt be found' />
    }

    let userComponent;

    if (user?.role === ERole.USER && validateUserSQL(user)) {
        userComponent = <SellerProfile seller={user} />;
    } else if (user?.role === ERole.ADMIN && validateUserSQL(user)) {
        userComponent = <AdminProfile seller={user} />;
    } else if (user?.role === ERole.MANAGER && validateUserSQL(user)) {
        userComponent = <ManagerProfile seller={user} />;
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
        </div>

    );
};

export { ProfilePage };

