import React, {FC} from 'react';
import {Outlet} from "react-router";
import Chat from "../components/Chat";

const MainLayout: FC = () => {
    return (
        <div>
            <h1>Main Layout</h1>
            <h1>Chat</h1>
            <Chat/>
            <Outlet/>
        </div>
    );
};

export {MainLayout};