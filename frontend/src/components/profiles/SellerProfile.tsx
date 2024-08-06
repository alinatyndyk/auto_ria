import { FC, useEffect, useState } from 'react';
import { useAppNavigate, useAppSelector } from '../../hooks';
import { IUserResponse } from "../../interfaces/user/seller.interface";

interface IProps {
    seller: IUserResponse;
}

const SellerProfile: FC<IProps> = ({ seller }) => {
    const navigate = useAppNavigate();
    const { premiumBoughtToggle } = useAppSelector(state => state.sellerReducer);

    const [displayedAccountType, setDisplayedAccountType] = useState<string>(seller.accountType);

    const {
        id, city, number, region, avatar, name, lastName, accountType, createdAt
    } = seller;

    let picture = avatar ?? "channels4_profile.jpg";

    useEffect(() => {
        // Update displayed account type if the user has bought premium
        if (premiumBoughtToggle && accountType === "BASIC") {
            setDisplayedAccountType("PREMIUM");
        }
    }, [premiumBoughtToggle, accountType]);

    const authNavigationComponent = (
        <div className="profile-actions">
            <button onClick={() => navigate('/profile/cars')}>My cars</button>
            <button onClick={() => navigate('/profile/premium')}>Premium and Cards</button>
            <button onClick={() => navigate('/profile/update')}>Update account info</button>
        </div>
    );

    const date = createdAt.slice(0, 10); // Adjusted to slice date properly
    const formattedNumbers = `${date.slice(0, 10)}`; // Format date properly

    return (
        <div>
            <div style={{ display: "flex", columnGap: "20px" }}>
                <img
                    style={{ height: "80px", width: "80px", borderRadius: "50%", marginRight: "10px" }}
                    src={`http://localhost:8080/users/avatar/${picture}`}
                    alt="Avatar"
                />
                <div>
                    <div>id: {id}</div>
                    <div>{name} {lastName}</div>
                    <div>✆ {number}</div>
                    <div style={{ fontSize: "9px" }}>◉ {region}, {city}</div>
                    <div>account: {displayedAccountType}</div>
                    <div>joined: {formattedNumbers}</div>
                </div>
            </div>

            <div className="header" style={{ marginBottom: "30px" }}>
                <div className="auth-links">{authNavigationComponent}</div>
            </div>
        </div>
    );
};

export { SellerProfile };
