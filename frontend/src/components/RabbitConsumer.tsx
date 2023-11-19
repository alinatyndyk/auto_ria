import React, {useEffect, useState} from 'react';
// @ts-ignore
import ampq from 'amqplib'

// const RabbitConsumer = () => {
//
//     const [getMessages, setMessages] = useState([]);
//
//     const messages: ((prevState: never[]) => never[]) | { content: { toString: () => any; }; }[] = [];
//
//     useEffect(() => {
//         const consumeMessage = async () => {
//             try {
//                 // Connect to RabbitMQ server
//                 // @ts-ignore
//                 const connection = await amqp.connect('amqp://localhost');
//
//                 // Create a channel
//                 const channel = await connection.createChannel();
//
//                 // Declare the queue you want to consume messages from
//                 const queue = 'newQueue';
//                 await channel.assertQueue(queue);
//
//                 // Consume messages from the queue
//                 await channel.consume(
//                     queue,
//                     (message: { content: { toString: () => any; }; }) => {
//                         // Handle received message
//                         if (message && message.content) {
//                             const content = message.content.toString();
//                             console.log(`Received message: ${content}`);
//                             messages.push(message);
//                         }
//                     },
//                     { noAck: true } // Remove this if you want to manually acknowledge the received messages
//                 );
//             } catch (error) {
//                 console.error('Error consuming message:', error);
//             }
//         };
//
//         consumeMessage();
//     }, []);
//
//     return <div>RabbitConsumer</div>;
// };
//
// export default RabbitConsumer;