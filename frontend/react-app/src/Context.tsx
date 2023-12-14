import {createContext, FC, useEffect, useState} from "react";
import {ICustomerResponse} from "./interfaces/user/customer.interface";
import {ISellerResponse} from "./interfaces/user/seller.interface";
import {IManagerResponse} from "./interfaces/user/manager.interface";
import {IAdminResponse} from "./interfaces/user/admin.interface";
import {sellerActions} from "./redux/slices/seller.slice";
import {useAppSelector} from "./hooks";

interface IChildren {
    children: any
}


const ThemeContext =
    createContext<any>({});

export const MyContextProvider: FC<IChildren> = ({children}) => {
    const {user} = useAppSelector(state => state.sellerReducer);
    const [theme, setTheme] =
        useState<ICustomerResponse | ISellerResponse | IManagerResponse | IAdminResponse | string>("");

    useEffect(() => {
        sellerActions.getByToken();
        if (user != null) {
            setTheme(user);
        }
    }, [user]);

    return (
        <ThemeContext.Provider value={theme}>
            {children}
        </ThemeContext.Provider>
    );
};

export {
    ThemeContext
}
