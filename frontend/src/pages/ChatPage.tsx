import { FC, useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate } from "react-router";
import { useAppDispatch, useAppSelector } from '../hooks';
import { IUserResponse } from '../interfaces/user/seller.interface';
import { chatActions } from '../redux/slices/chat.slice';
import { authService } from '../services';
import { securityService } from '../services/security.service';
import './Chat.css';
import ErrorForbidden from './error/ErrorForbidden';
import { MessageClass } from '../interfaces/chat.interface';

interface INewMessage {
    content: string;
}

const ChatPage: FC = () => {

    const navigate = useNavigate();
    const dispatch = useAppDispatch();

    const location = useLocation();

    const receiver = location.state?.user as IUserResponse | undefined;

    
    const { msgsOfChat, pageMsgsCurrent, pagesMsgsInTotal } = useAppSelector(state => state.chatReducer);
    const { user } = useAppSelector(state => state.sellerReducer);
    
    const [inputValue, setInputValue] = useState('');
    const [msg, setMsg] = useState<any[]>([]);
    const [msgChat, setMsgChat] = useState<any[]>([]);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(false);
    const [isFetching, setIsFetching] = useState(false);
    
    const chatContainerRef = useRef<HTMLDivElement>(null);
    
    const auth = authService.getAccessToken();
    
    const socket = useMemo(() => {
        if (receiver) {
            return new WebSocket(`ws://localhost:8080/chat?receiverId=${receiver.id}&auth=${auth}`);
        }
        return null;
    }, [auth, receiver]);
    
    const sendMessage = (message: INewMessage) => {
        if (socket && socket.readyState === WebSocket.OPEN) {
            
            let meEncr: IUserResponse | null = null;
            
            const meDecr = localStorage.getItem("authorization");
            if (meDecr !== null) {
                meEncr = securityService.decryptObject(meDecr);
            } else if (user !== null) {
                meEncr = user;
            }

            if (meEncr !== null && receiver) {
                const messageObject = {
                    content: message.content,
                    name: meEncr.name,
                    senderId: meEncr.id,
                    receiverId: receiver?.id,
                    time: new Date().toISOString()
                };
                socket.send(JSON.stringify(messageObject));
            } else {
                navigate("/errors/forbidden", { state: { cause: "Couldnt send message. Please log in" } })
                
            }

        }
    };
    
    const handleFormSubmit = (e: { preventDefault: () => void; }) => {
        e.preventDefault();
        sendMessage({ content: inputValue });
        setInputValue('');
    };
    
    useEffect(() => {
        if (receiver) {
            let meEncr: IUserResponse | null = null;
            
            const meDecr = localStorage.getItem("authorization");
            if (meDecr !== null) {
                meEncr = securityService.decryptObject(meDecr);
            } else if (user !== null) {
                meEncr = user;
            }
            if (meEncr !== null) {
                setIsLoading(true);
                dispatch(chatActions.getMsgsOfChat({ page, yourId: meEncr.id, secondId: receiver.id }))
                .finally(() => setIsLoading(false));
            } else {
                navigate("/errors/forbidden", { state: { cause: "Couldnt access messages. Please log in" } })
            }
        }
    }, [receiver, navigate, dispatch, page]);
    
    useEffect(() => {
        if (socket) {
            
            socket.onmessage = (event) => {
                const message = event.data;
                try {
                    const obj = JSON.parse(message);
                    
                    let sender: IUserResponse | null = null;
                    
                    const meDecr = localStorage.getItem("authorization");
                    if (meDecr !== null) {
                        sender = securityService.decryptObject(meDecr);
                    } else if (user !== null) {
                        sender = user;
                    }
                    
                    if (sender !== null) {
                        if (
                            (obj.senderId === receiver?.id && obj.receiverId === sender.id) ||
                            (obj.senderId === sender.id && obj.receiverId === receiver?.id)
                        ) {
                            setMsg(prevState => [...prevState, obj]);
                        }
                    } else {
                        navigate("/errors/forbidden", { state: { cause: "Couldnt access messages. Please log in" } })
                    }
                    
                } catch (error) {
                    console.error('Error parsing message:', error);
                }
            };
        }
        
        return () => {
            if (socket && socket.readyState !== WebSocket.CLOSED) {
                socket.close();
            }
        };
    }, [receiver, socket]);
    
    useEffect(() => {
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
        }
    }, [msg]);
    
    useEffect(() => {
        
        if (msgsOfChat.length > 0 && msgChat.length > 0) {
            
            const message1: MessageClass = msgsOfChat[0];
            
            let arrayNew: number[] = [parseInt(message1.receiverId),
                parseInt(message1.senderId)]
                
                let arrayOld: number[] = [parseInt(msgChat[0].receiverId),
                parseInt(msgChat[0].senderId)]
                
                let sortedArr1 = arrayNew.slice().sort((a, b) => a - b);
                let sortedArr2 = arrayOld.slice().sort((a, b) => a - b);
                
                if (JSON.stringify(sortedArr1) !== JSON.stringify(sortedArr2)) {
                    const uniqueMessages = msgsOfChat.filter(msg => !msgChat.some(existingMsg => existingMsg.id === msg.id));
                setMsgChat(uniqueMessages);
            } else {
                const uniqueMessages = msgsOfChat.filter(msg => !msgChat.some(existingMsg => existingMsg.id === msg.id));
                setMsgChat(prevState => [...uniqueMessages, ...prevState]);
            }

        } else {
            const uniqueMessages = msgsOfChat.filter(msg => !msgChat.some(existingMsg => existingMsg.id === msg.id));
            setMsgChat(prevState => [...uniqueMessages, ...prevState]);
        }
        
        
    }, [msgsOfChat]);
    
    const handleScroll = () => {
        if (chatContainerRef.current) {
            if (chatContainerRef.current.scrollTop === 0 && !isLoading && !isFetching && pageMsgsCurrent < pagesMsgsInTotal) {
                setIsFetching(true);
                setPage(prevPage => prevPage + 1);
            }
        }
    };
    
    useEffect(() => {
        const container = chatContainerRef.current;
        if (container) {
            container.addEventListener('scroll', handleScroll);
            return () => {
                container.removeEventListener('scroll', handleScroll);
            };
        }
    }, [isLoading, isFetching]);

    useEffect(() => {
        if (isFetching) {
            const timer = setTimeout(() => {
                setIsFetching(false);
            }, 500);
            return () => clearTimeout(timer);
        }
    }, [isFetching]);
    
    let currentUser: IUserResponse | null = null;
    
    const meDecr = localStorage.getItem("authorization");
    if (meDecr !== null) {
        currentUser = securityService.decryptObject(meDecr);
    } else if (user !== null) {
        currentUser = user;
    }

    if (receiver === undefined) {
        return <ErrorForbidden cause='Couldnt access chat imformation'/>
    }
    
    return (
        <div className="chat-wrapper">
            <div className="chat-header">Chat</div>
            <div className="chat-container" ref={chatContainerRef}>
                {isLoading && <div className="loading-spinner">Loading...</div>}
                {msgChat.map((message, index) => (
                    <div key={index} className="message-container">
                        <div className={currentUser !== null && message.senderId == currentUser.id ? 'message me' : 'message other'}>
                            {message.content} + {message.senderId}
                        </div>
                    </div>
                ))}
                {msg.map((message, index) => (
                    <div key={index} className="message-container">
                        <div className={currentUser !== null && message.senderId === currentUser.id ? 'message me' : 'message other'}>
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

export { ChatPage };