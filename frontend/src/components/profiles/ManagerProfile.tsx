import { faIdBadge, faMapMarkerAlt, faUserCircle } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { FC, useState } from 'react';
import { FindCarById } from '../../forms/car/FindCarById';
import { IUserResponse } from '../../interfaces/user/seller.interface';
import { useAppNavigate } from '../../hooks';

interface IProps {
    seller: IUserResponse;
}

const ManagerProfile: FC<IProps> = ({ seller }) => {
    const { id, avatar, name, lastName, city, region, createdAt } = seller;
    const picture = avatar === null ? 'channels4_profile.jpg' : avatar;
    const navigate = useAppNavigate();
    const date = createdAt.slice(0, 3);
    const formattedNumbers = `${date[0]}.${date[1]}.${date[2]}`;

    const [activeTab, setActiveTab] = useState<string>('car-panel');

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
            <div style={{ display: 'flex', justifyContent: "space-between", marginBottom: '20px' }}>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                    <div style={{
                        width: '80px',
                        height: '80px',
                        borderRadius: '50%',
                        overflow: 'hidden',
                        marginRight: '10px',
                        position: 'relative',
                    }}>
                        <img
                            style={{
                                width: '100%',
                                height: '100%',
                                objectFit: 'cover', // This ensures the image covers the circle
                                position: 'absolute',
                                top: '0',
                                left: '0',
                            }}
                            src={`http://localhost:8080/users/avatar/${picture}`}
                            alt="Avatar"
                        />
                    </div>
                    <div>
                        <div style={{ marginBottom: '5px' }}>
                            <FontAwesomeIcon icon={faIdBadge} style={{ marginRight: '5px' }} />
                            <span>ID: {id}</span>
                        </div>
                        <div style={{ marginBottom: '5px' }}>
                            <FontAwesomeIcon icon={faUserCircle} style={{ marginRight: '5px' }} />
                            <span>{name} {lastName}</span>
                        </div>
                        <div style={{ marginBottom: '5px' }}>
                            <FontAwesomeIcon icon={faMapMarkerAlt} style={{ marginRight: '5px' }} />
                            <span>{city}, {region}</span>
                        </div>
                        <div>Joined: {formattedNumbers}</div>
                    </div>
                </div>
                <div style={{ width: '300px' }}>
                    <FindCarById />
                </div>
            </div>
            <div className="header" style={{ marginBottom: "30px" }}>
                <div className="auth-links" style={{ display: 'flex', gap: '16px' }}>
                    <button
                        onClick={() => handleNavigation('/profile/car-panel')}
                        className={activeTab === '/profile/car-panel' ? 'active-tab' : ''}
                        style={{
                            height: '36px',
                            padding: '0 16px',
                            fontFamily: 'Montserrat, sans-serif',
                            color: activeTab === '/profile/car-panel' ? '#000' : '#333',
                            backgroundColor: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            fontSize: '0.9rem',
                            fontWeight: 300,
                            position: 'relative',
                            transition: 'color 0.3s ease',
                            outline: 'none'
                        }}
                    >
                        Generate code panel
                        <span style={{
                            content: '',
                            position: 'absolute',
                            bottom: 0,
                            left: 0,
                            width: '100%',
                            height: '1px',
                            backgroundColor: activeTab === '/profile/car-panel' ? '#000' : 'transparent',
                            transform: activeTab === '/profile/car-panel' ? 'scaleX(1)' : 'scaleX(0)',
                            transition: 'transform 0.3s ease, background-color 0.3s ease'
                        }} />
                    </button>
                    <button
                        onClick={() => handleNavigation('/profile/code-panel')}
                        className={activeTab === '/profile/code-panel' ? 'active-tab' : ''}
                        style={{
                            height: '36px',
                            padding: '0 16px',
                            fontFamily: 'Montserrat, sans-serif',
                            color: activeTab === '/profile/code-panel' ? '#000' : '#333',
                            backgroundColor: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            fontSize: '0.9rem',
                            fontWeight: 300,
                            position: 'relative',
                            transition: 'color 0.3s ease',
                            outline: 'none'
                        }}
                    >
                        Car instrument panel
                        <span style={{
                            content: '',
                            position: 'absolute',
                            bottom: 0,
                            left: 0,
                            width: '100%',
                            height: '1px',
                            backgroundColor: activeTab === '/profile/code-panel' ? '#000' : 'transparent',
                            transform: activeTab === '/profile/code-panel' ? 'scaleX(1)' : 'scaleX(0)',
                            transition: 'transform 0.3s ease, background-color 0.3s ease'
                        }} />
                    </button>
                    <button
                        onClick={() => handleNavigation('/profile/update')}
                        className={activeTab === '/profile/update' ? 'active-tab' : ''}
                        style={{
                            height: '36px',
                            padding: '0 16px',
                            fontFamily: 'Montserrat, sans-serif',
                            color: activeTab === '/profile/update' ? '#000' : '#333',
                            backgroundColor: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            fontSize: '0.9rem',
                            fontWeight: 300,
                            position: 'relative',
                            transition: 'color 0.3s ease',
                            outline: 'none'
                        }}
                    >
                        Change account info
                        <span style={{
                            content: '',
                            position: 'absolute',
                            bottom: 0,
                            left: 0,
                            width: '100%',
                            height: '1px',
                            backgroundColor: activeTab === '/profile/update' ? '#000' : 'transparent',
                            transform: activeTab === '/profile/update' ? 'scaleX(1)' : 'scaleX(0)',
                            transition: 'transform 0.3s ease, background-color 0.3s ease'
                        }} />
                    </button>
                </div>
            </div>
        </div>
    );
};

export { ManagerProfile };
