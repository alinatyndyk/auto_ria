import { FC, useEffect, useState } from 'react';
import { Outlet } from 'react-router-dom';
import { ERole } from '../constants/role.enum';
import { LogOutForm } from '../forms/auth/logs/LogOutForm';
import { useAppDispatch, useAppNavigate, useAppSelector } from '../hooks';
import { ISellerResponse } from '../interfaces/user/seller.interface';
import { sellerActions } from '../redux/slices/seller.slice';
import { securityService } from '../services/security.service';
import './MainLayout.css';
import Footer from '../components/Footer';

const MainLayout: FC = () => {
    const navigate = useAppNavigate();
    const dispatch = useAppDispatch();
    const storedAuth = localStorage.getItem('isAuth');
    const AuthObj = localStorage.getItem('authorization');

    const { errorDeleteById } = useAppSelector((state) => state.sellerReducer);
    const [profileInfo, setProfileInfo] = useState<ISellerResponse | null>(null);
    const [showResponse, setShowResponse] = useState<boolean>(false);
    const [showModal, setShowModal] = useState<boolean>(false);

    useEffect(() => {
        if (AuthObj !== null && storedAuth === 'true') {
            const decryptedAuth = securityService.decryptObject(AuthObj);
            setProfileInfo(decryptedAuth as ISellerResponse);
        } else {
            setProfileInfo(null); // Reset profile info if not authenticated
        }
    }, [AuthObj, storedAuth]);

    useEffect(() => {
        if (errorDeleteById) {
            setShowResponse(true);
            const timer = setTimeout(() => {
                setShowResponse(false);
            }, 5000);

            return () => clearTimeout(timer);
        }
    }, [errorDeleteById]);

    const deleteAccount = async () => {
        if (AuthObj !== null && storedAuth === 'true') {
            const decryptedAuth = securityService.decryptObject(AuthObj);
            if (decryptedAuth?.id) {
                const id: number = decryptedAuth.id;
                const { type } = await dispatch(sellerActions.deleteById(id));
                const lastWord = type.substring(type.lastIndexOf('/') + 1);
                return lastWord === 'fulfilled';
            }
        }
        return false;
    };

    const handleDeleteClick = () => {
        setShowModal(true);
    };

    const handleConfirmDelete = async () => {
        const success = await deleteAccount();
        if (success) {
            setShowModal(false);
            navigate('/cars');
        } else {
            setShowResponse(true);
            const timer = setTimeout(() => {
                setShowResponse(false);
                setShowModal(false); // Close the modal after showing the error for 5 seconds
            }, 5000);
        }
    };

    const handleCancelDelete = () => {
        setShowModal(false);
    };

    const authNavigationComponent = storedAuth === 'true' ? (
        <div className="profile-menu">
            <button 
                className="profile-button"
                onClick={() => navigate('/profile')}
            >
                {profileInfo?.avatar !== null ? (
                    <img
                        className="profile-avatar"
                        src={`http://localhost:8080/users/avatar/${profileInfo?.avatar}`}
                        alt="Avatar"
                    />
                ) : (
                    <img
                        className="profile-avatar"
                        src="http://localhost:8080/users/avatar/channels4_profile.jpg"
                        alt="Avatar"
                    />
                )}
                {profileInfo?.name}
            </button>
            <div className="dropdown-menu">
                <button onClick={() => navigate('/profile')}>Profile</button>
                <button onClick={handleDeleteClick}>Delete my account</button>
                <LogOutForm />
            </div>
        </div>
    ) : (
        <div className="auth-links">
            <button onClick={() => navigate(`auth/register/${ERole.USER}`)}>Register</button>
            <button onClick={() => navigate('/auth/login')}>Login</button>
        </div>
    );

    return (
        <div className="main-layout">
            <div className="header">
                <h3 onClick={() => navigate('/')}>Autoria</h3>
                <div className="auth-links">{authNavigationComponent}</div>
                {!storedAuth && <button onClick={() => navigate('/cars')}>Cars</button>}
            </div>
            <Outlet />
            <Footer />
            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h4>Are you sure you want to delete your account?</h4>
                        {showResponse && (
                            <div className="error-message">
                                {errorDeleteById?.message || 'Произошла ошибка при удалении аккаунта.'}
                            </div>
                        )}
                        <div className="modal-actions">
                            <button className="modal-button confirm" onClick={handleConfirmDelete}>
                                Delete
                            </button>
                            <button className="modal-button cancel" onClick={handleCancelDelete}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export { MainLayout };
