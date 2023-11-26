import React, {FC} from 'react';
import {Outlet} from "react-router";
import {ChatPage} from "../pages/ChatPage";

const MainLayout: FC = () => {
    return (
        <div>
            <h1>Main Layout</h1>
            <h1>Chat</h1>
            <Outlet/>
        </div>
    );
};

export {MainLayout};