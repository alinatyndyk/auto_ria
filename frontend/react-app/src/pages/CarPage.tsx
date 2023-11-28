import React, {FC} from 'react';
import {CarForm, Cars} from "../components/cars";
import {useAppNavigate} from "../hooks";

const CarPage: FC = () => {

    const navigate = useAppNavigate();

    return (
        <div>
            <CarForm/>
            <Cars sellerId={null}/>
            <button onClick={() => navigate('/register')}>register</button>
        </div>
    );
};

export default CarPage;