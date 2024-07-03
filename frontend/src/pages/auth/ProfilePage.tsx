import { FC, useEffect, useState } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { AdminProfile } from "../../components/profiles/AdminProfile";
import { ManagerProfile } from "../../components/profiles/ManagerProfile";
import { SellerProfile } from "../../components/profiles/SellerProfile";
import { ERole } from "../../constants/role.enum";
import { ChangePasswordForm } from "../../forms/auth/passwords/ChangePasswordForm";
import { useAppDispatch, useAppSelector } from "../../hooks";
import { IGeoCity, IGeoRegion } from '../../interfaces/geo.interface';
import { validateUserSQL } from '../../interfaces/user/joi/user.interface.joi';
import { IUserUpdateRequest } from '../../interfaces/user/seller.interface';
import { authActions } from "../../redux/slices";
import { sellerActions } from "../../redux/slices/seller.slice";
import { FindCarById } from '../../forms/car/FindCarById';
import { securityService } from '../../services/security.service';
import { error } from 'console';
import { CarForm } from '../../forms';
import { UpdateUserForm } from '../../forms/auth/logs/update/UpdateUserForm';


const ProfilePage: FC = () => {

    const dispatch = useAppDispatch();

    const { user } = useAppSelector(state => state.sellerReducer);

    useEffect(() => {
        setTimeout(() => {
        }, 400);
        dispatch(sellerActions.getByToken());
        if (user) {
            const obj = securityService.encryptObject(user)
            localStorage.setItem("authorization", obj);
        }
    }, []);


    let userComponent;

    if (user === null) {
        userComponent = <div>Loading...
            <button onClick={() => dispatch(authActions.refresh())}>refresh</button></div>;
    } else if (user.role === ERole.USER && validateUserSQL(user)) {
        userComponent = <SellerProfile seller={user} />;
    } else if (user.role === ERole.ADMIN && validateUserSQL(user)) {
        userComponent = <AdminProfile seller={user} />;
    } else if (user.role === ERole.MANAGER && validateUserSQL(user)) {
        userComponent = <ManagerProfile seller={user} />;
    } else {
        userComponent = <div>User type not recognized</div>;
    }

    return (
        <div>
            {userComponent}
        </div>

    );
};

export { ProfilePage };

