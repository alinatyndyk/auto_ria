import React, {useEffect, useMemo, useState} from 'react';
import {useAppDispatch, useAppSelector} from "../hooks";
import {sellerActions} from "../redux/slices/seller.slice";
import {IMessage} from "./cars";
import {authService} from "../services";

interface INewMessage {
    content: string;
}

function Chat() {

    const {messages} = useAppSelector(state => state.sellerReducer);
    const dispatch = useAppDispatch();

    const [inputValue, setInputValue] = useState('');
    const [getChatMessages, setChatMessages] = useState<IMessage[]>([]);
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
        const page: number = 0;
        dispatch(sellerActions.getChatMessages(page)); //todo 20 for element, when more -load another page
        //add to array [new messages + prev.state]
        setChatMessages(messages);
        console.log(getChatMessages)
        console.log("def 0")
    }, [])

    const [msg, setMsg] = useState([]);
    const auth = authService.getAccessToken();
    const receiver: number = 3; //from url params

    const socket = useMemo(() =>
        new WebSocket(`ws://localhost:8080/chat?receiverId=${receiver}&auth=${auth}`), [auth, receiver]);

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
    const getMore = (page: number) => {
        console.log("get more");
        dispatch(sellerActions.getChatMessages(page));
    }

    useEffect(() => {
        setChatMessages(prevState => [...messages, ...prevState]);
    }, [messages]);

    return (
        <div>
            <div>Chat</div>

            <button onClick={() => getMore(1)}>show more</button>
            {getChatMessages.map((message: IMessage, index) => (
                <div key={index}>{message.content}</div>
            ))}
            {/*<div>changes of message state</div>*/}
            {/*{messages.map((message: IMessage, index) => (*/}
            {/*    <div key={index}>{message.content}</div>*/}
            {/*))}*/}
            {msg.map((message, index) => (
                <div key={index}>{message}</div>
            ))}
            <form onSubmit={handleFormSubmit}>
                <input type="text" value={inputValue} onChange={(e) => setInputValue(e.target.value)}/>
                <button type="submit">Send</button>
            </form>
        </div>
    );
}

export {Chat};