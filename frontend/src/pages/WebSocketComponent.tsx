import React, { FC, useEffect, useMemo, useRef, useState } from 'react';
import { useLocation } from "react-router";
import { IMessage } from '../components/cars';
import { authService } from '../services';
import { IUserResponse } from '../interfaces/user/seller.interface';
import { securityService } from '../services/security.service';
import './Chat.css'; // Import the CSS file

interface INewMessage {
    content: string;
}

const Chat: FC = () => {
    const location = useLocation();
    const { user: receiver } = location.state as { user: IUserResponse };

    const [inputValue, setInputValue] = useState('');
    const [msg, setMsg] = useState<any[]>([]);

    const chatContainerRef = useRef<HTMLDivElement>(null);

    const auth = authService.getAccessToken();

    // Update WebSocket URL to use the actual receiver.id
    const socket = useMemo(() => new WebSocket(`ws://localhost:8080/chat?receiverId=${receiver.id}&auth=${auth}`), [auth, receiver.id]);

    const sendMessage = (message: INewMessage) => {
        if (socket.readyState === WebSocket.OPEN) {
            const me = localStorage.getItem("authorization") ?? '';
            const sender = securityService.decryptObject(me);
            const messageObject = {
                content: message.content,
                name: sender.name,
                senderId: sender.id,
                receiverId: receiver.id,
                time: new Date().toISOString()
            };
            socket.send(JSON.stringify(messageObject));
        }
    };

    const handleFormSubmit = (e: { preventDefault: () => void; }) => {
        e.preventDefault();
        sendMessage({ content: inputValue });
        setInputValue('');
    };

    useEffect(() => {
        socket.onopen = () => {
            console.log('WebSocket connected');
        };

        socket.onmessage = (event) => {
            const message = event.data;
            const obj = JSON.parse(message);
            const me = localStorage.getItem("authorization") ?? '';
            const sender = securityService.decryptObject(me);

            // Update the condition to check for correct sender and receiver
            if (
                (obj.senderId === receiver.id && obj.receiverId === sender.id) ||
                (obj.senderId === sender.id && obj.receiverId === receiver.id)
            ) {
                setMsg(prevState => [...prevState, obj]);
            }
        };

        socket.onclose = () => {
            console.log('WebSocket disconnected');
        };

        return () => {
            if (socket && socket.readyState !== WebSocket.CLOSED) {
                socket.close();
            }
        };
    }, [receiver.id, socket]);

    useEffect(() => {
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
        }
    }, [msg]);

    const me = localStorage.getItem("authorization") ?? '';
    const currentUser = securityService.decryptObject(me);

    return (
        <div className="chat-wrapper">
            <div className="chat-header">Chat</div>
            <div className="chat-container" ref={chatContainerRef}>
                {msg.map((message, index) => (
                    <div key={index} className="message-container">
                        <div className={message.senderId === currentUser.id ? 'message me' : 'message other'}>
                            {message.content}
                        </div>
                    </div>
                ))}
            </div>
            <form className="chat-footer" onSubmit={handleFormSubmit}>
                <input type="text" value={inputValue} onChange={(e) => setInputValue(e.target.value)} />
                <button disabled={!inputValue.trim().length} type="submit">Send</button>
            </form>
        </div>
    );
}

export { Chat };


