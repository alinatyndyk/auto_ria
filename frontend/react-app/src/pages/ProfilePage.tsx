import React, {FC, useEffect} from 'react';
import {useAppDispatch, useAppNavigate, useAppSelector} from "../hooks";
import {sellerActions} from "../redux/slices/seller.slice";
import {CustomerProfile} from "../components/profiles/CustomerProfile";
import {AdminProfile} from "../components/profiles/AdminProfile";
import {ManagerProfile} from "../components/profiles/ManagerProfile";
import {SellerProfile} from "../components/profiles/SellerProfile";
import {sellerService} from "../services/seller.service";
import {ChatPage} from "./ChatPage";

const ProfilePage: FC = () => {

    const {user, trigger} = useAppSelector(state => state.sellerReducer);

    const navigate = useAppNavigate();
    const dispatch = useAppDispatch();

    useEffect(() => {
        if (user === null) {
            dispatch(sellerActions.getByToken());
        }
    }, [user]);

    let userComponent;

    if (user === null) {
        userComponent = <div>Loading...</div>;
    } else if (user.role == "SELLER" && sellerService.isSellerResponse(user)) { //todo env and joi
        userComponent = <SellerProfile seller={user}/>;
    } else if (user.role == "CUSTOMER" && sellerService.isCustomerResponse(user)) {
        userComponent = <CustomerProfile seller={user}/>;
    } else if (user.role == "ADMIN" && sellerService.isAdminResponse(user)) {
        userComponent = <AdminProfile seller={user}/>;
    } else if (user.role == "MANAGER" && sellerService.isManagerResponse(user)) {
        userComponent = <ManagerProfile seller={user}/>;
    } else {
        userComponent = <div>User type not recognized</div>;
    }

    return (
        <div>
            <h1>Profile</h1>
            {userComponent}
            <ChatPage/>
        </div>
    );
};

export {ProfilePage};