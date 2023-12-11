import React, {FC} from 'react';
import {IManagerResponse} from "../../interfaces/user/manager.interface";

interface IProps {
    seller: IManagerResponse
}

const ManagerProfile: FC<IProps> = ({seller}) => {

    const {
        id, avatar, name, lastName,
    } = seller;
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
                <img style={{height: "80px"}} src={`http://localhost:8080/users/avatar/${avatar}`} alt="Avatar"/>
            </div>
            <br/>
        </div>
    );
};

export {ManagerProfile};