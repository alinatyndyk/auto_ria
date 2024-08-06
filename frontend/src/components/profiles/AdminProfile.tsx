import { faIdBadge, faKey, faMapMarkerAlt, faUserCircle } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { FC, useEffect, useState } from 'react';
import { useAppNavigate, useAppSelector } from '../../hooks';
import { IUserResponse } from '../../interfaces/user/seller.interface';

interface IProps {
    seller: IUserResponse;
}

const AdminProfile: FC<IProps> = ({ seller }) => {
    const { id, avatar, name, lastName, role, region, city, createdAt } = seller;
    const navigate = useAppNavigate();
    const { updateUserToggle, updateUser } = useAppSelector(state => state.sellerReducer);

    const [profile, setProfile] = useState<IUserResponse>(seller);
    const [activeTab, setActiveTab] = useState<string>('car-panel');

    const picture = profile.avatar ?? 'channels4_profile.jpg';
    const date = profile.createdAt.slice(0, 10);
    const formattedNumbers = `${date.slice(0, 10)}`;

    useEffect(() => {
        if (updateUserToggle) {
            setProfile(updateUser as IUserResponse);
        }
    }, [updateUserToggle, updateUser]);

    const handleNavigation = (path: string) => {
        navigate(path);
        setActiveTab(path);
    };

    return (
        <div style={{
            backgroundColor: 'whitesmoke',
            padding: '20px',
            borderRadius: '8px',
            fontSize: '16px',
        }}>
            <div style={{ display: 'flex', justifyContent: "space-between" }}>
                <div style={{ display: 'flex', justifyContent: "space-between" }}>
                    <div>
                        <img
                            style={{ height: '80px', width: '80px', borderRadius: '50%', marginRight: '10px' }}
                            src={`http://localhost:8080/users/avatar/${picture}`}
                            alt="Avatar"
                        />
                    </div>
                    <div>
                        <div style={{ marginBottom: '5px' }}>
                            <FontAwesomeIcon icon={faIdBadge} style={{ marginRight: '5px' }} />
                            <span>ID: {profile.id}</span>
                        </div>
                        <div style={{ marginBottom: '5px' }}>
                            <FontAwesomeIcon icon={faUserCircle} style={{ marginRight: '5px' }} />
                            <span>{profile.name} {profile.lastName}</span>
                        </div>
                        <div style={{ marginBottom: '5px' }}>
                            <FontAwesomeIcon icon={faKey} style={{ marginRight: '5px' }} />
                            <span>Access: {profile.role}</span>
                        </div>
                        <div style={{ marginBottom: '5px' }}>
                            <FontAwesomeIcon icon={faMapMarkerAlt} style={{ marginRight: '5px' }} />
                            <span>{profile.region}, {profile.city}</span>
                            <div>Joined: {formattedNumbers}</div>
                        </div>
                        <hr />
                    </div>
                </div>
            </div>
            <div className="header" style={{ marginBottom: "30px" }}>
                <div className="auth-links">
                    <button
                        onClick={() => handleNavigation('/profile/car-panel')}
                        className={activeTab === '/profile/car-panel' ? 'active-tab' : ''}
                    >
                        Generate code panel
                    </button>
                    <button
                        onClick={() => handleNavigation('/profile/code-panel')}
                        className={activeTab === '/profile/code-panel' ? 'active-tab' : ''}
                    >
                        Car instrument panel
                    </button>
                    <button
                        onClick={() => handleNavigation('/profile/update')}
                        className={activeTab === '/profile/update' ? 'active-tab' : ''}
                    >
                        Change account info
                    </button>
                </div>
            </div>
        </div>
    );
};

export { AdminProfile };
