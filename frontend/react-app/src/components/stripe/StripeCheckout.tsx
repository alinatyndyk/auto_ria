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
    const [getErrors, setErrors] = useState('');

    const payNow = async (token: any) => {
        try {
            const response =
                await axios.post("http://localhost:8080/payments/buy-premium", {
                    // await axios.post("http://localhost:8080/payments/add-payment-source", {
                    // id: "4",
                    token: token.id,
                    useDefaultCard: getUseDefaultCard,
                    setAsDefaultCard: getAsDefaultCard,
                    autoPay: autoPay
                }, {
                    headers: {
                        Authorization: `Bearer ${authService.getAccessToken()}`
                    }
                })
            console.log(response, "RESPONSE")
            if (response.status === 200) {
                window.location.reload();
            }
        } catch (e) {
            // @ts-ignore
            setErrors(e.response.data);
        }
    }

    const handleIsAutoPay = (event: { target: { checked: any; }; }) => {
        if (event.target.checked) {
            setAutoPay(true);
            setAsDefaultCard(true);
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
        } else if (autoPay) {
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
            <label>{JSON.stringify(getErrors)}</label>
            <label>
                <input checked={getAsDefaultCard} onChange={handlePayWithDefaultCard} type="checkbox"/> You want to pay
                with a default card of this
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
        paymentComponent = <div>
            <div style={{display: 'flex'}}>
                <label>{JSON.stringify(getErrors)}</label>
                <label>
                    <input checked={getAsDefaultCard} onChange={handleSetAsDefaultCard} type="checkbox"/> You want to
                    make this a
                    default card for this
                    account?
                    {autoPay ? <div style={{color: 'maroon'}}>Subscriptions *require* default cards for monthly
                        payments</div> : null}
                </label>
                <label>
                    <input onChange={handleIsAutoPay} type="checkbox"/> You want to be charged automatically and start a
                    subscription?
                </label>
            </div>
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
            height: "100px", width: "500px",
            fontSize: "9px",
            columnGap: "10px"
        }}>
            STRIPE
            {paymentComponent}
        </div>
    );
};

export {StripeCheckout};
