import React, {FC, useContext, useEffect, useState} from 'react';
import {useAppDispatch, useAppNavigate, useAppSelector} from "../../hooks";
import {sellerActions} from "../../redux/slices/seller.slice";
import {Outlet} from "react-router";
import {ICustomerChatResponse, ISellerChatResponse} from "../../interfaces/user/customer.interface";
import {ThemeContext} from "../../Context";
import {ERole} from "../../constants/role.enum";

const ChatPage: FC = () => {

    const theme = useContext(ThemeContext);

    const {chats, totalPages} = useAppSelector(state => state.sellerReducer);
    const [fetchedCustomers, setFetchedCustomers] = useState<ICustomerChatResponse[] | ISellerChatResponse[]>([]);

    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();

    const [getPage, setPage] = useState<number>(0);

    useEffect(() => {
        dispatch(sellerActions.getChatsByUserToken(getPage));
    }, [])


    useEffect(() => {
        chats.forEach((chat) => {
            if (theme.role == ERole.CUSTOMER) {
                console.log("in customer")
                dispatch(sellerActions.getSellerById(chat.sellerId))
                    .then((customer) => {

                        const type = customer.type;
                        const lastWord = type.substring(type.lastIndexOf("/") + 1);
                        // @ts-ignore
                        if (lastWord == "fulfilled" && !fetchedCustomers.some((c) => c.id === customer.payload.id)) {
                            // @ts-ignore
                            setFetchedCustomers(prevState => [...prevState, {
                                // @ts-ignore
                                ...customer.payload,
                                notSeen: chat.notSeenCustomer
                            }]); //display customer.payload in reverse
                        }
                    });
            } else if (theme.role == ERole.SELLER) {
                dispatch(sellerActions.getCustomerById(chat.customerId))
                    .then((customer) => {

                        const type = customer.type;
                        const lastWord = type.substring(type.lastIndexOf("/") + 1);
                        // @ts-ignore
                        if (lastWord == "fulfilled" && !fetchedCustomers.some((c) => c.id === customer.payload.id)) {
                            console.log("fulfilled did pass if");
                            setFetchedCustomers(prevState => [...prevState, {
                                // @ts-ignore
                                ...customer.payload,
                                notSeen: chat.notSeenSeller
                            }]);
                        }
                    })
            }
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
        setPage(prevState => prevState - 1);
    };

    const nextPage = () => {
        setPage(prevState => prevState + 1);
    };

    return (
        <div>
            Chat page
            {fetchedCustomers.length == 0 && <div style={{color: "blue"}}>No conversations started yet</div>}
            {fetchedCustomers.map((customer, index) => (
                <div style={{
                    backgroundColor: "whitesmoke",
                    fontSize: "9px",
                    width: "500px",
                    marginBottom: "10px",
                    padding: "20px",
                    borderRadius: "5px",
                    columnGap: "10px"
                }} key={index} onClick={() => navigate(`/chats/${customer.id}`)}>
                    <div>
                        <img height={"80px"} key={customer.avatar}
                             src={`http://localhost:8080/users/avatar/${customer.avatar}`} alt=''/>
                    </div>
                    <div>{customer.id}</div>
                    <div>NOT SEEN: {customer.notSeen}</div>
                    <div>{customer.name} {customer.lastName}</div>
                    <div>last online: {customer.lastOnline}</div>
                </div>
            ))}
            <div style={{display: 'flex'}}>
                <button disabled={getButtons} onClick={() => prevPage()}>prev</button>
                <button disabled={getNextButtons} onClick={() => nextPage()}>next</button>
                <div>total: {totalPages}</div>
            </div>
            <Outlet context={{
                senderRole: theme.role,
                senderId: theme.id,
            }}/>
        </div>
    );
};

export {ChatPage};