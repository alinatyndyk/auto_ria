import { FC, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../hooks';
import { ChatCard } from './ChatCard';
import { chatActions } from '../redux/slices/chat.slice';

const ChatsPage: FC = () => {

    const dispatch = useAppDispatch();

    const { chatsByUser, pageCurrent, pagesInTotal } = useAppSelector(state => state.chatReducer);

    useEffect(() => {
        dispatch(chatActions.getChatsByUser(0))
    }, [])

    return (
        <div>
            {chatsByUser.map(chat => <ChatCard key={chat.id} chat={chat} />)}
        </div>
    );
};

export { ChatsPage };

