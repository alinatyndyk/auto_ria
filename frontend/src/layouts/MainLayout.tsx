import React, { FC, useEffect, useState } from 'react';
import { Outlet } from 'react-router-dom';
import { ERole } from '../constants/role.enum';
import { LogOutForm } from '../forms/auth/logs/LogOutForm';
import { useAppDispatch, useAppNavigate, useAppSelector } from '../hooks';
import { sellerActions } from '../redux/slices/seller.slice';
import { securityService } from '../services/security.service';
import './MainLayout.css';

const MainLayout: FC = () => {
    const navigate = useAppNavigate();
    const dispatch = useAppDispatch();
    const storedAuth = localStorage.getItem('isAuth');
    const AuthObj = localStorage.getItem('authorization');

    const { errorDeleteById } = useAppSelector((state) => state.sellerReducer);
    const { isAuth } = useAppSelector((state) => state.authReducer);

    const deleteAccount = async () => {

        setTimeout(() => {}, 300);

        if (AuthObj !== null && storedAuth === 'true' && isAuth === true) {
            const decryptedAuth = securityService.decryptObject(AuthObj);
            if (decryptedAuth?.id) {
                const id: number = decryptedAuth.id;
                const { type } = await dispatch(sellerActions.deleteById(id));
                const lastWord = type.substring(type.lastIndexOf('/') + 1);
                if (lastWord === 'fulfilled') {
                    setTimeout(() => {
                        navigate('/cars');
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

    let authNavigationComponent;

    if (storedAuth === 'true') {
        authNavigationComponent = (
            <div className="profile-actions">
                <button onClick={() => navigate('/profile')}>Profile</button>
                <LogOutForm />
                <button onClick={deleteAccount}>Delete my account</button>
                {errorDeleteById && showResponse ? (
                    <div className="error-message">{errorDeleteById?.message}</div>
                ) : null}
            </div>
        );
    } else {
        authNavigationComponent = (
            <div className="auth-links">
                <button onClick={() => navigate(`auth/register/${ERole.USER}`)}>Register</button>
                <button onClick={() => navigate('/auth/login')}>Login</button>
            </div>
        );
    }

    return (
        <div className="main-layout">
            <div className="header">
                <h3 onClick={() => navigate('/')}>Autoria</h3>
                <div className="auth-links">{authNavigationComponent}</div>
                <button onClick={() => navigate('/cars')}>Cars</button>
            </div>
            <Outlet />
        </div>
    );
};

export { MainLayout };
