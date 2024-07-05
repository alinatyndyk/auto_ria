import { FC } from 'react';
import { IChatResponse } from '../interfaces/chat.interface';

interface IProps {
    chat: IChatResponse
}

const ChatFull: FC<IProps> = ({ chat }) => {
    // const { chatId } = useParams<{ chatId: string }>();

    return (
        <div>
            {JSON.stringify(chat)}
        </div>
    );
};

export { ChatFull };