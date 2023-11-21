import React, {useEffect, useState} from 'react';
import axios from "axios";

function RabbitMQConsumer() {

    const [msg, setMsg] = useState([]);
    const [chat, setChat] = useState([]);
    const [open, setOpen] = useState();
    const messages: any[] | React.SetStateAction<undefined> = [];


    const getChat = async () => {
        console.log("xxxxxxxxxxxxxxxxxxx");
        try {
            const response =
                // await axios.get("http://localhost:8080/cars/page/0")
                await axios.get("http://localhost:8080/chats/page/2?sellerId=7&customerId=3")
            console.log(response);
            console.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            setChat(response.data);
            console.log("response.data");
            ;
        } catch (e) {
            console.log(e, 'error');
            // @ts-ignore
            setErrors(e.response.data);
        }
    }


    useEffect(() => {
        const socket = new WebSocket('ws://localhost:8080/chat?customer=3&seller=7&state=seller'); // Replace with your actual WebSocket endpoint


        socket.onopen = () => {
            console.log('WebSocket connected');
        };

        socket.onmessage = (event) => {
            const message = event.data;
            console.log('Received message:', message);
            messages.push(message);
            // @ts-ignore
            setMsg(messages);
            // @ts-ignore
            setOpen(true);
            // @ts-ignore
            // setMsg(prevMsg => [...prevMsg, message]);
            // Process the received message as needed
        };
        console.log(msg)
        console.log("msg")

        socket.onclose = () => {
            console.log('WebSocket disconnected');
        };

    return () => {
        socket.close();
    };
    }, [msg]);

    return (
        <div>
            <div>Your component JSX</div>
            <div>THE CHAT</div>
            <div>{JSON.stringify(chat)}</div>
            <div>SOCKET</div>
            {msg.map((message, index) => (
                <div key={index}>{message}</div>
            ))}

            <button onClick={getChat}>get chat</button>

        </div>
    );
}

export default RabbitMQConsumer;