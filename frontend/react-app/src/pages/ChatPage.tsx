import React, {FC, useEffect, useState} from 'react';
import {IChatResponse} from "../interfaces/message.interface";
import {useAppDispatch, useAppNavigate, useAppSelector} from "../hooks";
import {sellerActions} from "../redux/slices/seller.slice";
import {Outlet} from "react-router";
import {ICustomerResponse} from "../interfaces/user/customer.interface";
import {carActions} from "../redux/slices";

const ChatPage: FC = () => {

    const {chats, totalPages} = useAppSelector(state => state.sellerReducer);
    const [fetchedCustomers, setFetchedCustomers] = useState<ICustomerResponse[]>([]);

    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();

    useEffect(() => {
        dispatch(sellerActions.getChatsByUserToken(0)); //todo pagination
    }, [])
    let [getPage, setPage] = useState<number>(0);


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
                        setFetchedCustomers(prevState => [...prevState, customer.payload]); //display customer.payload in reverse
                    }
                }).catch((error) => {
                console.log(error);
            });
        });
        if (getPage <= 0) {
            setButtons(true);
        } else {
            setButtons(false);
        }

        if (getPage + 1 >= totalPages) {
            setNextButtons(true);
        } else {
            setNextButtons(false);
        }
    }, [chats, getPage, totalPages]);

    const [getButtons, setButtons] = useState(true);
    const [getNextButtons, setNextButtons] = useState(false);

    const prevPage = () => {
        console.log("prev")
        setPage(prevState => prevState - 1);
    };

    const nextPage = () => {
        console.log("next")
        setPage(prevState => prevState + 1);
    };

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
                    <div>last online: {customer.lastOnline}</div>
                </div>
            ))}
            <div style={{display: 'flex'}}>
                <button disabled={getButtons} onClick={() => prevPage()}>prev</button>
                <button disabled={getNextButtons} onClick={() => nextPage()}>next</button>
                <div>total: {totalPages}</div>
            </div>
            <Outlet/>
        </div>
    );
};

export {ChatPage};