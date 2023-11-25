import React, {FC, useState} from 'react';
import axios from "axios";
import Stripe from "react-stripe-checkout";
import {ISellerResponse} from "../../interfaces/user/seller.interface";
import {authService} from "../../services";

interface IProps {
    seller: ISellerResponse
}

const StripeCheckout: FC<IProps> = ({seller}) => {

    const [getUseDefaultCard, setUseDefaultCard] = useState(false);
    const [getAsDefaultCard, setAsDefaultCard] = useState(false);
    const [autoPay, setAutoPay] = useState(false);

    const payNow = async (token: any) => {
        try {
            console.log("HELLOOOOOOOOOOOOOOOOOOOOOOOOO")
            console.log(token);
            const response =
                await axios.post("http://localhost:8080/payments/buy-premium", {
                    // await axios.post("http://localhost:8080/payments/add-payment-source", {
                    id: "4",
                    token: token.id,
                    useDefaultCard: getUseDefaultCard,
                    setAsDefaultCard: getAsDefaultCard,
                    autoPay: autoPay
                }, {
                    headers: {
                        // 'Content-type': 'multipart/form-data'
                        Authorization: `Bearer ${authService.getAccessToken()}`
                    }
                })
            if (response.status === 200) {
                console.log('Your payment was successful');
            }
        } catch (e) {
            console.log(e, 'error');
            // @ts-ignore
            setErrors(e.response.data);
        }
    }

    const handleIsAutoPay = (event: { target: { checked: any; }; }) => {
        if (event.target.checked) {
            setAutoPay(true);
        } else {
            setAutoPay(false);
        }
    };

    const handlePayWithDefaultCard = (event: { target: { checked: any; }; }) => {
        if (event.target.checked) {
            setUseDefaultCard(true);
        } else {
            setUseDefaultCard(false);
        }
    };

    const handleSetAsDefaultCard = (event: { target: { checked: any; }; }) => {
        if (event.target.checked) {
            setAsDefaultCard(true);
        } else {
            setAsDefaultCard(false);
        }
    };

    const stripeKeyPublish =
        'pk_test_51Nf481Ae4RILjJWGS16n8CI5yhK3nWg0kTMZVvRTOgMOY4KBlgI21EcPsSj9tY4tfDTQWrlh1v0egnN0ozBT9ATQ00kuhJ8UrS'

    let paymentComponent;

    if (seller.isPaymentResourcePresent) {
        paymentComponent = <div style={{display: 'flex'}}>
            <label>
                <input onChange={handlePayWithDefaultCard} type="checkbox"/> You want to pay with a default card of this
                account?
            </label>
            <label>
                <input onChange={handleIsAutoPay} type="checkbox"/> You want to be charged automatically and start a
                subscription?
            </label>
            <Stripe
                stripeKey={stripeKeyPublish}
                token={payNow}
            />
        </div>
    } else if (!seller.isPaymentResourcePresent) {
        paymentComponent = <div style={{display: 'flex'}}>
            <label>
                <input onChange={handleSetAsDefaultCard} type="checkbox"/> You want to make this a default card for this
                account?
            </label>
            <label>
                <input onChange={handleIsAutoPay} type="checkbox"/> You want to be charged automatically and start a
                subscription?
            </label>
            <Stripe
                stripeKey={stripeKeyPublish}
                token={payNow}
            />
        </div>
    } else {
        paymentComponent = <div>Could not extract payment method...</div>
    }

    return (
        <div style={{
            backgroundColor: "whitesmoke",
            height: "110px", width: "220px",
            fontSize: "9px",
            columnGap: "10px"
        }}>
            STRIPE
            {paymentComponent}
        </div>
    );
};

export {StripeCheckout};
