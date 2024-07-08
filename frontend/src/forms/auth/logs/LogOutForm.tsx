import React, { FC } from 'react';
import { useAppDispatch, useAppNavigate, useAppSelector } from "../../../hooks";
import { authActions } from "../../../redux/slices";

const LogOutForm: FC = () => {
    const dispatch = useAppDispatch();

    const navigate = useAppNavigate();
    const logOut = async () => {

        try {
            await dispatch(authActions.signOut()).unwrap();
            navigate('/cars');
        } catch (err) {
            //todo to login and delete all tokens
            navigate('/errors/forbidden', { state: { cause: 'Auth token is either fully expired, invalid, or doesn\'t exist' } });        }

    }

    return (
        <div>
            <button onClick={logOut}>Sign Out</button>
        </div>
    );
};

export { LogOutForm };