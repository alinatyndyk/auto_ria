import React, {FC} from 'react';
import {Outlet} from "react-router";
import {useAppNavigate} from "../hooks";
import {Link} from "react-router-dom";
import {ERole} from "../constants/role.enum";
import {LogOutForm} from "../forms/auth/logs/LogOutForm";

const MainLayout: FC = () => {

    let authNavigationComponent;
    const storedAuth = localStorage.getItem('isAuth');

    if (storedAuth === "true") {
        authNavigationComponent = <div>
            <button onClick={() => navigate("/profile")}>profile</button>
            <LogOutForm/>
        </div>
    } else {
        authNavigationComponent = <div style={{display: "flex"}}>
            <div style={{display: "flex", columnGap: "20px"}}>
                <Link to={`auth/register/${ERole.SELLER}`}>register seller</Link>
                <Link to={`auth/register/${ERole.CUSTOMER}`}>register customer</Link>
                <Link to={`auth/register/${ERole.ADMIN}`}>register admin</Link>
                <Link to={`auth/register/${ERole.MANAGER}`}>register manager</Link>
                <button style={{
                    alignItems: "center",
                    width: "100px",
                    height: "30px",
                    color: "white",
                    border: "none",
                    backgroundColor: "green",
                }} onClick={() => navigate("/auth/login")}>Login
                </button>
            </div>
        </div>
    }

    const navigate = useAppNavigate();
    return (
        <div>
            <div style={{display: 'flex', backgroundColor: "whitesmoke", justifyContent: "space-between"}}>
                <h3 onClick={() => navigate("/")}>Autoria</h3>
                <button style={{height: "30px"}} onClick={() => navigate('/cars')}>cars</button>
                {authNavigationComponent}
            </div>
            <Outlet/>
        </div>
    );
};

export {MainLayout};