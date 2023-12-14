import React, {FC} from 'react';
import {IAdminResponse} from "../../interfaces/user/admin.interface";
import {GenerateManagerForm} from "../../forms/auth/codes/GenerateManagerForm";
import {GenerateAdminForm} from "../../forms/auth/codes/GenerateAdminForm";
import {CarForm} from "../../forms";
import {Cars} from "../cars";
import CarPage from "../../pages/CarPage";

interface IProps {
    seller: IAdminResponse
}

const AdminProfile: FC<IProps> = ({seller}) => {

    const {
        id, avatar, name, lastName, email, role
    } = seller;
    let picture;
    if (avatar == null) {
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
                    <img style={{height: "80px", width: "80px", borderRadius: "50%", marginRight: "10px"}}
                         src={`http://localhost:8080/users/avatar/${picture}`} alt="Avatar"/></div>
                <div>id: {id}</div>
                <div>{name} {lastName}</div>
                <div>{email}</div>
                <div>access: {role}</div>
            </div>
            <br/>
            <GenerateManagerForm/>
            <br/>
            <GenerateAdminForm/>
            <br/>
            <div>Create new car</div>
            <CarForm/>
            <CarPage/>
        </div>
    );
};

export {AdminProfile};