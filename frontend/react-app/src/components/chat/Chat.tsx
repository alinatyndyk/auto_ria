import React, {FC, useEffect, useMemo, useState} from 'react';
import {useAppDispatch, useAppSelector} from "../../hooks";
import {sellerActions} from "../../redux/slices/seller.slice";
import {IMessage} from "../cars";
import {authService} from "../../services";
import {useParams} from "react-router";

interface INewMessage {
    content: string;
}

const Chat: FC = () => {

    const {messages, chatPage, totalPages, chats} = useAppSelector(state => state.sellerReducer);
    const dispatch = useAppDispatch();

    const [inputValue, setInputValue] = useState('');
    const [getMoreBtn, setMoreBtn] = useState(false);
    const [getChatMessages, setChatMessages] = useState<IMessage[]>([]);

    const {receiverId} = useParams<{ receiverId: string }>();

    const sendMessage = (message: INewMessage) => {
        if (socket.readyState === WebSocket.OPEN) {
            socket.send(message.content);
        }
    };

    const handleFormSubmit = (e: { preventDefault: () => void; }) => {
        e.preventDefault();
        sendMessage({content: inputValue});
        setInputValue('');
    };

    useEffect(() => {
        dispatch(sellerActions.getChatMessages(chatPage));
        setChatMessages(messages);
    }, [])

    const [msg, setMsg] = useState([]);
    const auth = authService.getAccessToken();

    const socket = useMemo(() =>
        new WebSocket(`ws://localhost:8080/chat?receiverId=${receiverId}&auth=${auth}`), [auth, receiverId]);

    useEffect(() => {

        socket.onopen = () => {
            console.log('WebSocket connected');
        };

        socket.onmessage = (event) => {
            const message = event.data;
            console.log('Received message:', message);
            //@ts-ignore
            setMsg(prevState => [...prevState, message]);
        };

        socket.onclose = () => {
            console.log('WebSocket disconnected');
        }

        return () => {
            if (socket && socket.readyState !== WebSocket.CLOSED) {
                socket.close();
            }
        };

    }, []);
    const getMore = () => {
        const page = chatPage + 1;
        dispatch(sellerActions.getChatMessages(page));
    }

    useEffect(() => {
        if (getChatMessages.length > 0 && messages.length == 0) {
            setMoreBtn(true);
        } else {
            setChatMessages(prevState => [...[...messages].reverse(), ...prevState]);
        }
    }, [messages]);

    return (
        <div>
            <div>Chat</div>
            <button disabled={getMoreBtn} onClick={() => getMore()}>show more</button>
            {getChatMessages.map((message: IMessage, index) => (
                <div key={index}>{message.content}</div>
            ))}
            {msg.map((message, index) => (
                <div key={index}>{message}</div>
            ))}
            <form onSubmit={handleFormSubmit}>
                <input type="text" value={inputValue} onChange={(e) => setInputValue(e.target.value)}/>
                <button disabled={!inputValue.trim().length} type="submit">Send</button>
            </form>
        </div>
    );
}

export {Chat};