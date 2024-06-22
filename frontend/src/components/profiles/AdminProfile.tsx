import React, {FC} from 'react';
import {IAdminResponse} from "../../interfaces/user/admin.interface";
import {GenerateManagerForm} from "../../forms/auth/codes/GenerateManagerForm";
import {GenerateAdminForm} from "../../forms/auth/codes/GenerateAdminForm";
import CarPage from "../../pages/car/CarPage";
import { IUserResponse } from '../../interfaces/user/seller.interface';
import { Cars } from '../cars';
import { CarForm } from '../../forms/car/CarForm';

interface IProps {
    seller: IUserResponse
}


const AdminProfile: FC<IProps> = ({seller}) => {

    const {
        id, avatar, name, lastName, role
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
                <div>access: {role}</div>
            </div>
            <br/>
            <GenerateManagerForm/>
            <br/>
            <GenerateAdminForm/>
            <br/>
            <div>Create new car</div>
            <CarForm/>
        </div>
    );
};

export {AdminProfile};