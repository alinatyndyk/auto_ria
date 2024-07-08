import { FC } from 'react';
import { IUserResponse } from '../../interfaces/user/seller.interface';
import { FindCarById } from '../../forms/car/FindCarById';
import { disconnect } from 'process';

interface IProps {
    seller: IUserResponse
}

const ManagerProfile: FC<IProps> = ({ seller }) => {

    const {
        id, avatar, name, lastName, city, region, createdAt
    } = seller;

    let picture;
    if (avatar === null) {
        picture = "channels4_profile.jpg";
    } else {
        picture = avatar;
    }

    const date = createdAt.slice(0, 3);
    const formattedNumbers = `${date[0]}.${date[1]}.${date[2]}`;

    return (
        <div style={{ padding: '20px', backgroundColor: '#f0f0f0' }}>
            <div style={{
                display: "flex",
                backgroundColor: "white",
                height: "110px",
                width: "300px",
                fontSize: "12px",
                columnGap: "10px",
                boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
                borderRadius: "10px",
                padding: "10px",
                alignItems: "center"
            }}>
                <div style={{ marginRight: "10px" }}>
                    <img
                        style={{ height: "80px", width: "80px", borderRadius: "50%", boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)" }}
                        src={`http://localhost:8080/users/avatar/${picture}`}
                        alt="Avatar"
                    />
                </div>
                <div>
                    <div style={{ fontWeight: "bold", marginBottom: "5px" }}>Manager</div>
                    <div>id: {id}</div>
                    <div>{name} {lastName}</div>
                    <div>{city} - {region}</div>
                    <div>joined: {formattedNumbers}</div>
                </div>
            </div>
            <div style={{ marginTop: "20px" }}>
                <FindCarById />
            </div>
        </div>
    );
};

export { ManagerProfile };
