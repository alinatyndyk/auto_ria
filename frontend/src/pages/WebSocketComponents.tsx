import { FC, useEffect, useState } from 'react';
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

    const { chatsByUser, pagesInTotal } = useAppSelector(state => state.chatReducer);
    const { user } = useAppSelector(state => state.sellerReducer);

    const [getUsers, setUsers] = useState<IUserResponse[]>([]);
    const [getPage, setPage] = useState<number>(1);

    useEffect(() => {
        dispatch(chatActions.getChatsByUser(getPage - 1));
    }, [getPage]);

    useEffect(() => {
        if (chatsByUser.length > 0) {
            let meEncr: IUserResponse | null = null;
            const meDecr = localStorage.getItem("authorization");
            if (meDecr !== null) {
                meEncr = securityService.decryptObject(meDecr);
            } else if (user !== null) {
                meEncr = user;
            } else {
                navigate("/errors/forbidden", { state: { cause: "Couldn't access profile. Please log in" } });
                return;
            }

            const newUsers: IUserResponse[] = [];
            let promises: Promise<any>[] = [];

            chatsByUser.forEach(chat => {
                chat.users.forEach(userId => {
                    if (userId !== meEncr!.id && userId !== user?.id) {
                        const promise = dispatch(sellerActions.getUserConversation(userId)).then((res) => {
                            newUsers.push(res.payload as IUserResponse);
                        });
                        promises.push(promise);
                    }
                });
            });

            Promise.all(promises).then(() => setUsers(newUsers));
        } else {
            setUsers([]);
        }
    }, [chatsByUser, dispatch, navigate, user]);

    const prevPage = () => {
        if (getPage > 1) {
            setPage(prevState => prevState - 1);
        }
    };

    const nextPage = () => {
        if (getPage < pagesInTotal) {
            setPage(prevState => prevState + 1);
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
                <button onClick={prevPage} disabled={getPage === 1} style={{ backgroundColor: '#007bff', color: '#ffffff', padding: '10px 20px', margin: '0 10px', borderRadius: '4px', border: 'none', cursor: 'pointer', boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)', outline: 'none', opacity: getPage === 1 ? 0.5 : 1 }} className={getPage === 1 ? "disabled" : ""}>Previous</button>
                <button onClick={nextPage} disabled={getPage === pagesInTotal} style={{ backgroundColor: '#007bff', color: '#ffffff', padding: '10px 20px', margin: '0 10px', borderRadius: '4px', border: 'none', cursor: 'pointer', boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)', outline: 'none', opacity: getPage === pagesInTotal ? 0.5 : 1 }} className={getPage === pagesInTotal ? "disabled" : ""}>Next</button>
            </div>
            <div style={{ color: '#999999', textAlign: 'center', marginTop: '10px' }}>
                {`Page ${getPage} of ${pagesInTotal}`}
            </div>
            {selectedChat && <Chat key={selectedChat?.id} chat={selectedChat} />}
        </div>
    );
};

export { ChatsPage };




