import React, {FC} from 'react';
import { IUserResponse } from '../../interfaces/user/seller.interface';

interface IProps {
    seller: IUserResponse
}

const ManagerProfile: FC<IProps> = ({seller}) => {

    const {
        id, avatar, name, lastName,
    } = seller;

    let picture;
    if (avatar === null) {
        picture = "channels4_profile.jpg";
    } else {
        picture = avatar;
    }
    return (
        <div style={{
            display: "flex",
            backgroundColor: "whitesmoke",
            height: "110px", width: "220px",
            fontSize: "9px",
            columnGap: "10px"
        }}>
            <div>
                Manager
                <div>id: {id}</div>
                <div>{name} {lastName}</div>
                <img style={{height: "80px", width: "80px", borderRadius: "50%", marginRight: "10px"}}
                     src={`http://localhost:8080/users/avatar/${picture}`} alt="Avatar"/>            </div>
            <br/>
        </div>
    );
};

export {ManagerProfile};