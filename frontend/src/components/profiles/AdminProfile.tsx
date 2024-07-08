import { faIdBadge, faKey, faMapMarkerAlt, faUserCircle } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { FC } from 'react';
import { GenerateAdminForm } from '../../forms/auth/codes/GenerateAdminForm';
import { GenerateManagerForm } from '../../forms/auth/codes/GenerateManagerForm';
import { CarForm } from '../../forms/car/CarForm';
import { FindCarById } from '../../forms/car/FindCarById';
import { IUserResponse } from '../../interfaces/user/seller.interface';

interface IProps {
    seller: IUserResponse;
}

const AdminProfile: FC<IProps> = ({ seller }) => {
    const { id, avatar, name, lastName, role, region, city, createdAt } = seller;

    const picture = avatar === null ? 'channels4_profile.jpg' : avatar;

    const date = createdAt.slice(0, 3);
    const formattedNumbers = `${date[0]}.${date[1]}.${date[2]}`;

    return (
        <div style={{
            backgroundColor: 'whitesmoke',
            padding: '20px',
            borderRadius: '8px',
            gridTemplateColumns: 'auto 1fr',
            columnGap: '20px',
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
                            <span>ID: {id}</span>
                        </div>
                        <div style={{ marginBottom: '5px' }}>
                            <FontAwesomeIcon icon={faUserCircle} style={{ marginRight: '5px' }} />
                            <span>{name} {lastName}</span>
                        </div>
                        <div style={{ marginBottom: '5px' }}>
                            <FontAwesomeIcon icon={faKey} style={{ marginRight: '5px' }} />
                            <span>Access: {role}</span>
                        </div>
                        <div style={{ marginBottom: '5px' }}>
                            <FontAwesomeIcon icon={faMapMarkerAlt} style={{ marginRight: '5px' }} />
                            <span>{region}, {city}</span>
                            <div>joined : {formattedNumbers}</div>
                        </div>
                        <hr />
                        <br />
                        <FindCarById />
                        <br />
                        <GenerateManagerForm />
                        <GenerateAdminForm />
                    </div>
                </div>
                <div style={{ marginTop: '20px' }}>
                    <CarForm />
                </div>
            </div>
        </div>
    );
};
export { AdminProfile };


