import { FC, useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '../hooks';
import { IUserResponse } from '../interfaces/user/seller.interface';
import { chatActions } from '../redux/slices/chat.slice';
import { sellerActions } from '../redux/slices/seller.slice';
import { securityService } from '../services/security.service';
import { ChatCard } from './ChatCard';

const ChatsPage: FC = () => {

    const dispatch = useAppDispatch();

    const { chatsByUser, pageCurrent, pagesInTotal, } = useAppSelector(state => state.chatReducer);
    const { userConvesation } = useAppSelector(state => state.sellerReducer);

    const [getUsers, setUsers] = useState<IUserResponse[]>([]);

    useEffect(() => {
        dispatch(chatActions.getChatsByUser(0));
    }, []);

    useEffect(() => {
        if (chatsByUser.length > 0) {
            const meDecr = localStorage.getItem("authorization") ?? "";
            const meEncr = securityService.decryptObject(meDecr);

            chatsByUser.forEach(chat => {
                chat.users.forEach(userId => {
                    if (userId !== meEncr.id && !getUsers.find(user => user.id === userId)) {
                        dispatch(sellerActions.getUserConversation(userId));
                    }
                });
            });
        }
    }, [chatsByUser, getUsers]);

    useEffect(() => {
        if (userConvesation && !getUsers.find(user => user.id === userConvesation.id)) {
            setUsers(prevState => [...prevState, userConvesation]);
        }
    }, [userConvesation, getUsers]);




    return (
        <div>
            {getUsers.map(chat => <ChatCard key={chat.id} chat={chat} />)}
        </div>
    );
};

export { ChatsPage };

