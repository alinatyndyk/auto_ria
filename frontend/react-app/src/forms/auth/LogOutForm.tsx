import React, {FC} from 'react';
import {useAppDispatch} from "../../hooks";
import {authActions} from "../../redux/slices";

const LogOutForm: FC = () => {
    const dispatch = useAppDispatch();

    const logOut = async () => {

        await dispatch(authActions.signOut());
    }

    return (
        <div>
            <button onClick={logOut}>Sign Out</button>
        </div>
    );
};

export {LogOutForm};