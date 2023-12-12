import React, {FC, useEffect, useState} from 'react';
import {IChatResponse} from "../interfaces/message.interface";
import {useAppDispatch, useAppNavigate, useAppSelector} from "../hooks";
import {sellerActions} from "../redux/slices/seller.slice";
import {Outlet} from "react-router";
import {ICustomerResponse} from "../interfaces/user/customer.interface";

const ChatPage: FC = () => {

    const {chats} = useAppSelector(state => state.sellerReducer);
    const [fetchedCustomers, setFetchedCustomers] = useState<ICustomerResponse[]>([]);

    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();

    useEffect(() => {
        dispatch(sellerActions.getChatsByUserToken(0)); //todo pagination
    }, [])


    useEffect(() => {
        console.log("new load")
        chats.forEach((chat) => {
            dispatch(sellerActions.getCustomerById(chat.customerId))
                .then((customer) => {

                    const type = customer.type;
                    const lastWord = type.substring(type.lastIndexOf("/") + 1);

                    // @ts-ignore
                    if (lastWord == "fulfilled" && !fetchedCustomers.some((c) => c.id === customer.payload.id)) {
                        // @ts-ignore
                        setFetchedCustomers(prevState => [...prevState, customer.payload]);
                    }
                }).catch((error) => {
                console.log(error);
            });
        });
    }, [chats]);

    return (
        <div>
            Chat page

            {fetchedCustomers.map((customer, index) => (
                <div key={index} onClick={() => navigate(`/chats/${customer.id}`)}>
                    <div>
                        <img height={"80px"} key={customer.avatar}
                             src={`http://localhost:8080/users/avatar/${customer.avatar}`} alt=''/>
                    </div>
                    <div>{customer.id}</div>
                    <div>{customer.name} {customer.lastName}</div>
                    <div>online {customer.lastOnline}</div>
                </div>
            ))}

            <Outlet/>
        </div>
    );
};

export {ChatPage};