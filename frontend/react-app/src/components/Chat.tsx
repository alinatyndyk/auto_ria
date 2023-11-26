import React, {useEffect, useMemo, useState} from 'react';
import {useAppDispatch, useAppSelector} from "../hooks";
import {sellerActions} from "../redux/slices/seller.slice";
import {IMessage} from "./cars";
import {authService} from "../services";
import {SubmitHandler, useForm} from "react-hook-form";

interface INewMessage {
    content: string;
}

function Chat() {

    const {messages} = useAppSelector(state => state.sellerReducer);
    const dispatch = useAppDispatch();
    const {reset, handleSubmit, register} = useForm<INewMessage>();

    const [inputValue, setInputValue] = useState('');

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
    }, [])

    const [msg, setMsg] = useState([]);
    // const auth: string = `Bearer ` + authService.getAccessToken();
    const auth = authService.getAccessToken();
    console.log(auth);
    const receiver: number = 3; //from url params

    const socket = useMemo(() =>
        new WebSocket(`ws://localhost:8080/chat?receiverId=${receiver}&auth=${auth}`), [auth, receiver]);

    useEffect(() => {

        socket.onopen = () => {
            console.log('WebSocket connected');
            socket.send("hello 27") //trigger
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

    const send: SubmitHandler<INewMessage> = (message: INewMessage) => {
        socket.send(message.content);
    };

    return (
        <div>
            <div>Chat</div>
            {messages.map((message: IMessage, index) => (
                <div key={index}>{message.content}</div>
            ))}
            {msg.map((message, index) => (
                <div key={index}>{message}</div>
            ))}
            {/*<form onSubmit={handleSubmit(send)}>*/}
            {/*    <input type="text" placeholder={"Write a message..."}/>*/}
            {/*    <button>send</button>*/}
            {/*</form>*/}

            <form onSubmit={handleFormSubmit}>
                <input type="text" value={inputValue} onChange={(e) => setInputValue(e.target.value)}/>
                <button type="submit">Send</button>
            </form>
        </div>
    );
}

export {Chat};