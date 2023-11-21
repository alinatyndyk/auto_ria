import React, {FC} from 'react';
import {CarForm, Cars} from "../components/cars";
import {useAppSelector} from "../hooks";

const CarPage: FC = () => {
    return (
        <div>
            <CarForm/>
            <Cars/>
        </div>
    );
};

export default CarPage;