import React, {FC} from 'react';
import {Outlet} from "react-router";
import {useAppNavigate, useAppSelector} from "../hooks";

const MainLayout: FC = () => {

    const {isAuth} = useAppSelector(state => state.authReducer);
    let authNavigationComponent;

    console.log(isAuth, "is auth")
    if (isAuth) {
        authNavigationComponent = <div>
            <button onClick={() => navigate("/profile")}>Login</button>
        </div>
    } else {
        authNavigationComponent = <div>
            <button onClick={() => navigate("/auth/login")}>Login</button>
            <button onClick={() => navigate("/auth/register")}>Register</button>
        </div>
    }

    const navigate = useAppNavigate();
    return (
        <div>
            <div style={{display: 'flex', backgroundColor: "whitesmoke"}}>
                <h3 onClick={() => navigate("/")}>Autoria</h3>
                {authNavigationComponent}
            </div>
            <Outlet/>
        </div>
    );
};

export {MainLayout};