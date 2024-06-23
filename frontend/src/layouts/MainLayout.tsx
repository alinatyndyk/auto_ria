import { FC, useState } from 'react';
import { Outlet } from "react-router";
import { Link } from "react-router-dom";
import { ERole } from "../constants/role.enum";
import { LogOutForm } from "../forms/auth/logs/LogOutForm";
import { useAppDispatch, useAppNavigate, useAppSelector } from "../hooks";
import { sellerActions } from '../redux/slices/seller.slice';
import { securityService } from '../services/security.service';

const MainLayout: FC = () => {

    let authNavigationComponent;
    const dispatch = useAppDispatch();
    const storedAuth = localStorage.getItem('isAuth');
    const AuthObj = localStorage.getItem('authorization');

    const { errorDeleteById } = useAppSelector(state => state.sellerReducer);

    const [getDeleteResponse, setDeleteResponse] = useState('');

    const deleteAccount = async () => {
        if (AuthObj !== null) {
            const decryptedAuth = securityService.decryptObject(AuthObj);

            if (decryptedAuth?.id) {
                const id: number = decryptedAuth.id;
                await dispatch(sellerActions.deleteById(id));
                if (errorDeleteById?.message) {
                    setDeleteResponse(errorDeleteById.message);
                } 
                // else {
                //     localStorage.clear();
                //     navigate("/cars");
                // }
            }
        }
    };


    if (storedAuth === "true") {
        authNavigationComponent = (
            <div>
                <button onClick={() => navigate("/profile")}>Profile</button>
                <LogOutForm />
                <button onClick={() => deleteAccount()}>Delete my account</button>
                <div>{getDeleteResponse && <div style={{ fontSize: "7px", color: "red" }}>{getDeleteResponse}</div>}</div>
            </div>
        );
    } else {
        authNavigationComponent = <div style={{ display: "flex" }}>
            <div style={{ display: "flex", columnGap: "20px" }}>
                <Link to={`auth/register/${ERole.USER}`}>register seller</Link>
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
