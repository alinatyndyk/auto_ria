import { FC } from 'react';
import { IChatResponse } from '../interfaces/chat.interface';

interface IProps {
    chat: IChatResponse
}

const ChatCard: FC<IProps> = ({ chat }) => {

    return (
        <div>
            {JSON.stringify(chat)}
        </div>
    );
};

export { ChatCard };
