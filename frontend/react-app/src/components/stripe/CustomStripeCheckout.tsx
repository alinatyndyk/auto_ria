import React from 'react';
import {loadStripe} from '@stripe/stripe-js';
import {CardCvcElement, CardElement, CardExpiryElement, CardNumberElement, Elements} from '@stripe/react-stripe-js';

const stripePromise = loadStripe('pk_test_51Nf481Ae4RILjJWGS16n8CI5yhK3nWg0kTMZVvRTOgMOY4KBlgI21EcPsSj9tY4tfDTQWrlh1v0egnN0ozBT9ATQ00kuhJ8UrS');

const CustomStripeCheckout: React.FC = () => {
    return (
        <Elements stripe={stripePromise}>
            <div>Hello</div>
            <CardNumberElement/>
            <CardExpiryElement/>
            <CardCvcElement/>
            {/*<CardElement/>*/}
            {/* Your custom checkout form goes here */}
        </Elements>
    );
};

export {CustomStripeCheckout};