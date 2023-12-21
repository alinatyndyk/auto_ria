import React from 'react';
import './App.css';
import {Navigate, Route, Routes} from "react-router";
import {MainLayout} from "./layouts";
import CarPage from "./pages/car/CarPage";
import {RegisterPage} from "./pages/auth/RegisterPage";
import {LoginPage} from "./pages";
import {ProfilePage} from "./pages/auth/ProfilePage";
import {ForgotPasswordForm} from "./forms/auth/passwords/ForgotPasswordForm";
import {Chat} from "./components/chat/Chat";
import {ChatPage} from "./pages/chat/ChatPage";
import {ResetPasswordForm} from "./forms/auth/passwords/ResetPasswordForm";
import {ActivateForm} from "./forms/auth/activate/ActivateForm";
import {CarFull} from "./components/cars/CarFull";
import {LogOutForm} from "./forms/auth/logs/LogOutForm";

function App() {
    return (
        <Routes>
            <Route index element={<Navigate to={'cars'}/>}/>
            <Route path={'/'} element={<MainLayout/>}>
                <Route path={'cars'} element={<CarPage/>}/>
                <Route path={'cars/:carId'} element={<CarFull/>}/>
                <Route path={'auth/login'} element={<LoginPage/>}/>
                <Route path={'auth/register/:role'} element={<RegisterPage/>}/>
                <Route path={'profile'} element={<ProfilePage/>}/>
                <Route path={'chats'} element={<ChatPage/>}>
                    <Route path={':receiverId'} element={<Chat/>}/>
                </Route>
                <Route path={'auth/forgot-password/'} element={<ForgotPasswordForm/>}/>
                <Route path={'auth/reset-password/'} element={<ResetPasswordForm/>}/>
                <Route path={'auth/activate-account/:role'} element={<ActivateForm/>}/>
            </Route>
        </Routes>
    );
}

export default App;
