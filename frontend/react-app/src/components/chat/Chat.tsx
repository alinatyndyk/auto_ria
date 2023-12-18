import React, {FC, useEffect, useMemo, useState} from 'react';
import {useAppDispatch, useAppSelector} from "../../hooks";
import {sellerActions} from "../../redux/slices/seller.slice";
import {IMessage} from "../cars";
import {authService} from "../../services";
import {useOutletContext, useParams} from "react-router";
import {ERole} from "../../constants/role.enum";
import {IGetChatMessagesOutlet} from "../../interfaces/message.interface";
import {securityService} from "../../services/security.service";

interface INewMessage {
    content: string;
}

const Chat: FC = () => {

    const {messages, chatPageMessages, totalPagesMessages, chats} = useAppSelector(state => state.sellerReducer);
    const dispatch = useAppDispatch();

    const [inputValue, setInputValue] = useState('');
    const [getMoreBtn, setMoreBtn] = useState(false);
    const [getChatMessages, setChatMessages] = useState<IMessage[]>([]);

    const [getSellerId, setSellerId] = useState<string>();
    const [getCustomerId, setCustomerId] = useState<string>();

    const {receiverId} = useParams<{ receiverId: string }>();

    if (receiverId == undefined) {
        throw new Error("define a receiver")
    }

    const outletContext = useOutletContext<IGetChatMessagesOutlet>();

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

    let sellerId: string;
    let customerId: string;

    useEffect(() => {

        if (outletContext.senderId != undefined) {
            const secret = securityService.encryptObject(outletContext);
            console.log(secret, "secret")
            localStorage.setItem('chat-info-outlet', securityService.encryptObject(outletContext));
        }

        const outlet = localStorage.getItem('chat-info-outlet');

        let outletParse: IGetChatMessagesOutlet;

        if (outlet != null) {
            outletParse = securityService.decryptObject(outlet);
        } else {
            throw new Error("Chat info absent");
        }
        if (outletParse.senderRole == ERole.CUSTOMER) {
            customerId = outletParse.senderId;
            sellerId = receiverId;
        } else if (outletParse.senderRole == ERole.SELLER) {
            sellerId = outletParse.senderId;
            customerId = receiverId;
        } else {
            throw new Error("Could not execute chat info");
        }

        if (sellerId && customerId) {
            console.log(sellerId, customerId, "ids");
            dispatch(sellerActions.getChatMessages({
                page: chatPageMessages,
                sellerId: sellerId,
                customerId: customerId
            }));
            setChatMessages([...messages]);
        }

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
        const page = chatPageMessages + 1;

        const outlet = localStorage.getItem('chat-info-outlet');

        let outletParse: IGetChatMessagesOutlet;

        if (outlet != null) {
            outletParse = securityService.decryptObject(outlet);
            if (outletParse.senderRole == ERole.CUSTOMER) {
                customerId = outletParse.senderId;
                sellerId = receiverId;
            } else if (outletParse.senderRole == ERole.SELLER) {
                sellerId = outletParse.senderId;
                customerId = receiverId;
            } else {
                throw new Error("Could not execute chat info");
            }
        }

        if (sellerId && customerId) {
            console.log(sellerId, customerId, "xxx");
            dispatch(sellerActions.getChatMessages({page: page, sellerId: sellerId, customerId: customerId}));
        }
    }

    useEffect(() => {
        if (getChatMessages.length > 0 && messages.length == 0) {
            setMoreBtn(true);
        } else {

            setChatMessages(prevState => [...[...messages], ...prevState]);
        }
    }, [messages]);

    return (
        <div>
            <div>Chat</div>
            <button disabled={getMoreBtn} onClick={() => getMore()}>show more</button>
            {getChatMessages.map((message: IMessage, index) => (
                <div key={index}>{message.content}
                    <button>edit</button>
                </div>
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