import React, {FC} from 'react';
import {CarForm, Cars} from "../components/cars";
import {useAppNavigate} from "../hooks";

const CarPage: FC = () => {

    const navigate = useAppNavigate();

    return (
        <div>
            <Cars sellerId={null}/>
        </div>
    );
};

export default CarPage;