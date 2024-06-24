import React, { FC } from 'react';
import { useAppDispatch, useAppNavigate, useAppSelector } from "../../../hooks";
import { authActions } from "../../../redux/slices";

const LogOutForm: FC = () => {
    const dispatch = useAppDispatch();
    const { signOutErrors } = useAppSelector(state => state.authReducer);


    const navigate = useAppNavigate();
    const logOut = () => {

        dispatch(authActions.signOut());

        if (signOutErrors === null) {
            navigate('/auth/login');
        }
    }

    return (
        <div>
            <button onClick={logOut}>Sign Out</button>
        </div>
    );
};

export { LogOutForm };