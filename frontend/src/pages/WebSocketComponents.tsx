import React, { FC, useEffect, useState } from 'react';
import { useAppDispatch, useAppNavigate, useAppSelector } from '../hooks';
import { IUserResponse } from '../interfaces/user/seller.interface';
import { chatActions } from '../redux/slices/chat.slice';
import { sellerActions } from '../redux/slices/seller.slice';
import { securityService } from '../services/security.service';
import { ChatCard } from './ChatCard';
import { Chat } from './WebSocketComponent';

const ChatsPage: FC = () => {
    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();

    const { chatsByUser, pageCurrent, pagesInTotal } = useAppSelector(state => state.chatReducer);
    const { userConvesation, user } = useAppSelector(state => state.sellerReducer);

    const [getUsers, setUsers] = useState<IUserResponse[]>([]);
    const [currentPage, setCurrentPage] = useState<number>(0);

    useEffect(() => {
        dispatch(chatActions.getChatsByUser(currentPage));
    }, []);

    useEffect(() => {
        if (chatsByUser.length > 0) {
            let meEncr: IUserResponse;
            const meDecr = localStorage.getItem("authorization");
            if (meDecr !== null) {
                meEncr = securityService.decryptObject(meDecr);
            } else if (user !== null) {
                meEncr = user;
            } else {
                navigate("/errors/forbidden", { state: { cause: "Couldnt access profile. Please log in" } });
            }

            chatsByUser.forEach(chat => {
                chat.users.forEach(userId => {
                    if ((userId !== meEncr.id || userId !== user?.id) && !getUsers.find(user => user.id === userId)) {
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

    const handleNextPage = () => {
        if (currentPage < pagesInTotal - 1) {
            setCurrentPage(prevPage => prevPage + 1);
        }
    };

    const handlePrevPage = () => {
        if (currentPage > 0) {
            setCurrentPage(prevPage => prevPage - 1);
        }
    };

    const [selectedChat, setSelectedChat] = useState<IUserResponse>();

    return (
        <div style={{ padding: '20px', backgroundColor: '#f0f0f0', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)' }}>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '20px' }}>
                {getUsers.map(chat => (
                    <div onClick={() => {
                        setSelectedChat(chat);
                    }} key={chat.id} style={{ backgroundColor: '#ffffff', padding: '15px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)', minWidth: '200px', width: 'calc(33.33% - 20px)' }}>
                        <ChatCard key={chat.id} chat={chat} />
                    </div>
                ))}
            </div>
            <div style={{ textAlign: 'center', marginTop: '20px' }}>
                <button onClick={handlePrevPage} disabled={currentPage === 0} style={{ backgroundColor: '#007bff', color: '#ffffff', padding: '10px 20px', margin: '0 10px', borderRadius: '4px', border: 'none', cursor: 'pointer', boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)', outline: 'none', opacity: currentPage === 0 ? 0.5 : 1 }} className={currentPage === 0 ? "disabled" : ""}>Previous</button>
                <button onClick={handleNextPage} disabled={currentPage === pagesInTotal - 1} style={{ backgroundColor: '#007bff', color: '#ffffff', padding: '10px 20px', margin: '0 10px', borderRadius: '4px', border: 'none', cursor: 'pointer', boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)', outline: 'none', opacity: currentPage === pagesInTotal - 1 ? 0.5 : 1 }} className={currentPage === pagesInTotal - 1 ? "disabled" : ""}>Next</button>
            </div>
            <div style={{ color: '#999999', textAlign: 'center', marginTop: '10px' }}>
                {`Page ${currentPage + 1} of ${pagesInTotal}`}
            </div>
            {selectedChat && <Chat key={selectedChat?.id} chat={selectedChat} />}
        </div>
    );
};

export { ChatsPage };



