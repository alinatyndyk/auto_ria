import React, {FC} from 'react';
import {useAppDispatch, useAppNavigate} from "../../../hooks";
import {authActions} from "../../../redux/slices";

const LogOutForm: FC = () => {
    const dispatch = useAppDispatch();

    const navigate = useAppNavigate();
    const logOut = async () => {

        await dispatch(authActions.signOut());
        navigate('/auth/login');
    }

    return (
        <div>
            <button onClick={logOut}>Sign Out</button>
        </div>
    );
};

export {LogOutForm};