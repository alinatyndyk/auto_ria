import { Navigate, Route, Routes } from "react-router";
import './App.css';
import { CarFull } from "./components/cars/CarFull";
import { ActivateForm } from "./forms/auth/activate/ActivateForm";
import { ForgotPasswordForm } from "./forms/auth/passwords/ForgotPasswordForm";
import { ResetPasswordForm } from "./forms/auth/passwords/ResetPasswordForm";
import { MainLayout } from "./layouts";
import { LoginPage } from "./pages";
import { RegisterPage } from "./pages/auth/RegisterPage";
import CarPage from './pages/car/CarPage';
import { ProfilePage } from "./pages/auth/ProfilePage";
import ErrorForbidden from "./pages/error/ErrorForbidden";
function App() {
    return (
        <Routes>
            <Route index element={<Navigate to={'cars'} />} />
            <Route path={'/'} element={<MainLayout />}>
                <Route path={'cars'} element={<CarPage/>} />
                <Route path={'cars/:carId'} element={<CarFull />} />
                <Route path={'auth/login'} element={<LoginPage />} />
                <Route path={'auth/register/:role'} element={<RegisterPage/>} />
                <Route path={'profile'} element={<ProfilePage />} />
                <Route path={'auth/forgot-password/'} element={<ForgotPasswordForm />} />
                <Route path={'auth/reset-password/'} element={<ResetPasswordForm />} />
                <Route path={'auth/activate-account/:role'} element={<ActivateForm />} />
                <Route path={'errors/forbidden'} element={<ErrorForbidden cause="Forbidden access."/>} />
            </Route>
        </Routes>
    );
}

export default App;
