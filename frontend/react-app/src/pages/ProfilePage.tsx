import React, {FC, useEffect} from 'react';
import {useAppDispatch, useAppNavigate, useAppSelector} from "../hooks";
import {useParams} from "react-router";
import {sellerActions} from "../redux/slices/seller.slice";
import {AdminResponse} from "../interfaces/class/AdminResponseClass";
import {SellerResponse} from "../interfaces/class/SellerResponseClass";
import {CustomerResponse} from "../interfaces/class/CustomerResponseClass";
import {ManagerResponse} from "../interfaces/class/ManagerResponseClass";
import {SellerProfile} from "../components/profiles/SellerProfile";
import {CustomerProfile} from "../components/profiles/CustomerProfile";
import {AdminProfile} from "../components/profiles/AdminProfile";
import {ManagerProfile} from "../components/profiles/ManagerProfile";

const ProfilePage: FC = () => {

    // const {user, trigger} = useAppSelector(state => state.sellerReducer);

    const location = useLocation();
    const user = location.state;

    const navigate = useAppNavigate();
    const dispatch = useAppDispatch();
    const {id} = useParams<{ id: string }>();

    useEffect(() => {
        dispatch(sellerActions.getById(Number(id)));
    }, [dispatch])

    let userComponent;

    if (user instanceof SellerResponse) {
        userComponent = (
            <div>
                <SellerProfile seller={user}/>
            </div>
        );
    } else if (user instanceof CustomerResponse) {
        userComponent = (
            <div>
                <CustomerProfile seller={user}/>
            </div>
        );
    } else if (user instanceof AdminResponse) {
        userComponent = (
            <div>
                <AdminProfile seller={user}/>
            </div>
        );
    } else if (user instanceof ManagerResponse) {
        userComponent = (
            <div>
                <ManagerProfile seller={user}/>
            </div>
        );
    } else {
        userComponent = <div>User type not recognized</div>;
    }


    return (
        <div>
            Profile
            {userComponent}
        </div>
    );
};

export {ProfilePage};