import React, { FC } from 'react';
import { useAppNavigate } from '../hooks';
import { IUserResponse } from '../interfaces/user/seller.interface';
import './ChatCard.css';

interface IProps {
    chat: IUserResponse;
}

const ChatCard: FC<IProps> = ({ chat }) => {
    const navigate = useAppNavigate();

    const picture = chat.avatar === null ? 'channels4_profile.jpg' : chat.avatar;

    return (
        <div onClick={() => navigate("/chat", { state: { user: chat } })} className="chat-card-container">
            <div className="chat-card-photo">
                <img src={`http://localhost:8080/users/avatar/${picture}`} alt="User Avatar" />
            </div>
            <div className="chat-card-details">
                <div className="chat-card-name">{chat.name} {chat.lastName}</div>
                <div className="chat-card-location">{chat.city} -- {chat.region}</div>
            </div>
        </div>
    );
};

export { ChatCard };



