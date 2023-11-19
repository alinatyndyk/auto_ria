import React, {useEffect, useState} from 'react';

function RabbitMQConsumer() {

        const [msg, setMsg] = useState([]);
        const [open, setOpen] = useState();
        const messages: any[] | React.SetStateAction<undefined> = [];


    useEffect(() => {
        const socket = new WebSocket('ws://localhost:8080/chat'); // Replace with your actual WebSocket endpoint


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

        // return () => {
        //     socket.close();
        // };
    }, [msg]);

    return (
        <div>
            {/* Your component JSX */}
            {/*{msg}*/}
            {msg.map((message, index) => (
                <div key={index}>{message}</div>
            ))}

        </div>
    );
}

export default RabbitMQConsumer;
