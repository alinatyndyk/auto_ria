import React, {FC} from 'react';
import {CarForm, Cars} from "../components/cars";
import {useAppNavigate} from "../hooks";
import {useParams} from "react-router";
import {CustomStripeCheckout} from "../components/stripe/CustomStripeCheckout";
import {StripeCheckout} from "../components/stripe/StripeCheckout";

const CarPage: FC = () => {

    const navigate = useAppNavigate();

    return (
        <div>
            {/*<CustomStripeCheckout />*/}
            <CarForm/>
            <Cars sellerId={null}/>
            <button onClick={() => navigate('/register')}>register</button>
        </div>
    );
};

export default CarPage;