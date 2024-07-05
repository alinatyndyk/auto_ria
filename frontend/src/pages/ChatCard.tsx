import { FC } from 'react';
import { useAppNavigate, useAppSelector } from '../hooks';
import { IUserResponse } from '../interfaces/user/seller.interface';
import { Chat } from './WebSocketComponent';

interface IProps {
    chat: IUserResponse
}

const ChatCard: FC<IProps> = ({ chat }) => {

    const navigate = useAppNavigate();

    const { userConvesation: user } = useAppSelector(state => state.sellerReducer);


    return (
        <div onClick={() => {
            console.log(`=> Your chat with ${user?.name}`)
            navigate("/chat", { state: { user: chat } });
        }}>
            <hr />
            <div>{chat?.id}</div>
            <div className="car-photo">
                <img src={`http://localhost:8080/users/avatar/${user?.avatar}`} alt="" />
            </div>
            <div>{chat?.name} {chat?.lastName}</div>
            <div>{chat?.number}</div>
            <div>{chat?.city} -- {chat?.region}</div>
        </div>
    );
};

export { ChatCard };

