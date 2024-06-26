import { FC } from 'react';
import { GenerateAdminForm } from "../../forms/auth/codes/GenerateAdminForm";
import { GenerateManagerForm } from "../../forms/auth/codes/GenerateManagerForm";
import { CarForm } from '../../forms/car/CarForm';
import { IUserResponse } from '../../interfaces/user/seller.interface';

interface IProps {
    seller: IUserResponse
}


const AdminProfile: FC<IProps> = ({ seller }) => {


    const {
        id, avatar, name, lastName, role, region, city
    } = seller;
    let picture;
    if (avatar === null) {
        picture = "channels4_profile.jpg";
    } else {
        picture = avatar;
    }
    return (
        <div style={{
            backgroundColor: "whitesmoke",
            fontSize: "9px",
            columnGap: "10px"
        }}>
            <div>
                <div>
                    <img style={{ height: "80px", width: "80px", borderRadius: "50%", marginRight: "10px" }}
                        src={`http://localhost:8080/users/avatar/${picture}`} alt="Avatar" /></div>
                <div>id: {id}</div>
                <div>{name} {lastName}</div>
                <div>access: {role}</div>
                <div>{region} -- {city}</div>
            </div>
            <br />
            <GenerateManagerForm />
            <br />
            <GenerateAdminForm />
            <br />
            <div>Create new car</div>
            <CarForm />
        </div>
    );
};

export { AdminProfile };

