import React, {FC} from 'react';
import {Cars} from "../../components/cars";
import {useAppNavigate} from "../../hooks";

const CarPage: FC = () => {
    return (
        <div>
            <Cars sellerId={null}/>
        </div>
    );
};

export default CarPage;