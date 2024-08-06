import { faBolt, faCar, faCircle, faDollarSign, faFileAlt, faGlobe } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { FC } from 'react';
import { useAppNavigate } from "../../hooks";
import { CarsResponse } from "../../interfaces";
import './Car.css';

interface IProps {
    car: CarsResponse
}

const Car: FC<IProps> = ({ car }) => {
    const navigate = useAppNavigate();

    const {
        id, city, currency,
        model, photo, powerH,
        price, region, brand
    } = car;

    return (
        <div className="car-card" onClick={() => navigate(`/cars/${id}`)}>
            <div className="car-photo">
                <img src={`http://localhost:8080/users/avatar/${photo[0]}`} alt="" />
            </div>
            <div className="car-details">
                <div>{car.user.id} USER</div>
                <div><FontAwesomeIcon icon={faCircle} /> {id}</div>
                <div><FontAwesomeIcon icon={faCar} /> {brand}</div>
                <div><FontAwesomeIcon icon={faFileAlt} /> {model}</div>
                <div><FontAwesomeIcon icon={faBolt} /> {powerH}</div>
                <div className="price">
                    <FontAwesomeIcon icon={faDollarSign} /> {price} {currency}
                </div>
                <div className="location">
                    <FontAwesomeIcon icon={faGlobe} /> {region}, {city}
                </div>
            </div>
        </div>
    );
};

export { Car };