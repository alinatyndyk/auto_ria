import { FC, useEffect, useState } from 'react';
import { Outlet } from "react-router";
import { Link } from "react-router-dom";
import { ERole } from "../constants/role.enum";
import { LogOutForm } from "../forms/auth/logs/LogOutForm";
import { useAppDispatch, useAppNavigate, useAppSelector } from "../hooks";
import { sellerActions } from '../redux/slices/seller.slice';
import { securityService } from '../services/security.service';

const MainLayout: FC = () => {

    let authNavigationComponent;
    const navigate = useAppNavigate();
    const dispatch = useAppDispatch();
    const storedAuth = localStorage.getItem('isAuth');
    const AuthObj = localStorage.getItem('authorization');

    const { errorDeleteById } = useAppSelector(state => state.sellerReducer);

    const deleteAccount = async () => {
        console.log(AuthObj, storedAuth);
        if (AuthObj !== null && storedAuth === "true") {
            const decryptedAuth = securityService.decryptObject(AuthObj);
            if (decryptedAuth?.id) {
                const id: number = decryptedAuth.id;
                const { type } = await dispatch(sellerActions.deleteById(id));
                const lastWord = type.substring(type.lastIndexOf("/") + 1);
                if (lastWord === "fulfilled") {
                    setTimeout(() => {
                        navigate("/cars");
                      }, 300);
                }
            }
        }
    };

    const [showResponse, setShowResponse] = useState<boolean>(false);

    useEffect(() => {
        if (errorDeleteById != null) {
            setShowResponse(true);
            const timer = setTimeout(() => {
                setShowResponse(false);
            }, 5000);

            return () => clearTimeout(timer);

        }
    }, [errorDeleteById]);


    if (storedAuth === "true") {
        authNavigationComponent = (
            <div>
                <button onClick={() => navigate("/profile")}>Profile</button>
                <LogOutForm />
                <button onClick={() => deleteAccount()}>Delete my account</button>
                <div>
                    {errorDeleteById && showResponse ? (
                        <div style={{
                            color: "darkred"
                        }}>{errorDeleteById?.message}</div>
                    ) : null}
                </div>
            </div>
        );
    } else {
        authNavigationComponent = <div style={{ display: "flex" }}>
            <div style={{ display: "flex", columnGap: "20px" }}>
                <Link to={`auth/register/${ERole.USER}`}>Register</Link>
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

    return (
        <div>
            <div style={{ display: 'flex', backgroundColor: "whitesmoke", justifyContent: "space-between" }}>
                <h3 onClick={() => navigate("/")}>Autoria</h3>
                <button style={{ height: "30px" }} onClick={() => navigate('/cars')}>cars</button>
                {authNavigationComponent}
            </div>
            <Outlet />
        </div>
    );
};

export { MainLayout };

