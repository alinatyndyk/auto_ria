import React, {FC, useEffect} from 'react';
import {useAppDispatch, useAppSelector} from "../../hooks";
import {sellerActions} from "../../redux/slices/seller.slice";
import {CustomerProfile} from "../../components/profiles/CustomerProfile";
import {AdminProfile} from "../../components/profiles/AdminProfile";
import {ManagerProfile} from "../../components/profiles/ManagerProfile";
import {SellerProfile} from "../../components/profiles/SellerProfile";
import {sellerService} from "../../services/seller.service";
import {ERole} from "../../constants/role.enum";
import {authActions} from "../../redux/slices";
import {ChangePasswordForm} from "../../forms/auth/passwords/ChangePasswordForm";

const ProfilePage: FC = () => {

    const {user} = useAppSelector(state => state.sellerReducer);

    const dispatch = useAppDispatch();

    useEffect(() => {
        if (user === null) {
            dispatch(sellerActions.getByToken());
        }
    }, [user]);

    let userComponent;

    if (user === null) {
        userComponent = <div>Loading...
            <button onClick={() => dispatch(authActions.refresh())}>refresh</button></div>;
    } else if (user.role == ERole.SELLER && sellerService.isSellerResponse(user)) { //todo joi
        userComponent = <SellerProfile seller={user}/>;
    } else if (user.role == ERole.CUSTOMER && sellerService.isCustomerResponse(user)) {
        userComponent = <CustomerProfile seller={user}/>;
    } else if (user.role == ERole.ADMIN && sellerService.isAdminResponse(user)) {
        userComponent = <AdminProfile seller={user}/>;
    } else if (user.role == ERole.MANAGER && sellerService.isManagerResponse(user)) {
        userComponent = <ManagerProfile seller={user}/>;
    } else {
        userComponent = <div>User type not recognized</div>;
    }

    return (
        <div>
            <div>Profile</div>
            {userComponent}
            <ChangePasswordForm/>
        </div>
    );
};

export {ProfilePage};