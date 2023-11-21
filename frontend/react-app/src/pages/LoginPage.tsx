import React, {FC} from 'react';
import {CarForm, Cars} from "../components/cars";
import {useAppSelector} from "../hooks";
import {LoginForm} from "../forms";

const CarPage: FC = () => {
    return (
        <div>
            <LoginForm/>
        </div>
    );
};

export default CarPage;