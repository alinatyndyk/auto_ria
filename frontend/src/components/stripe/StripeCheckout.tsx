import axios from "axios";
import { FC, useState } from 'react';
import Stripe from "react-stripe-checkout";
import { IUserResponse } from "../../interfaces/user/seller.interface";
import { authService } from "../../services";

interface IProps {
    seller: IUserResponse
}

const StripeCheckout: FC<IProps> = ({ seller }) => {

    const [getUseDefaultCard, setUseDefaultCard] = useState(false);
    const [getAsDefaultCard, setAsDefaultCard] = useState(false);
    const [autoPay, setAutoPay] = useState(false);
    const [getErrors, setErrors] = useState();

    const payNow = async (token: any) => {
        try {
            const response =
                await axios.post("http://localhost:8080/payments/buy-premium", {
                    token: token.id,
                    useDefaultCard: getUseDefaultCard,
                    setAsDefaultCard: getAsDefaultCard,
                    autoPay: autoPay
                }, {
                    headers: {
                        Authorization: `Bearer ${authService.getAccessToken()}`
                    }
                })
            if (response.status === 200) {
                window.location.reload();
            }
        } catch (e) {
            // @ts-ignore
            setErrors(e.response.data);
        }
    }

    const addCard = async (token: any) => {
        try {
            const response =
                await axios.post("http://localhost:8080/payments/add-payment-source", {
                    token: token.id
                }, {
                    headers: {
                        Authorization: `Bearer ${authService.getAccessToken()}`
                    }
                })
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

    const handleIsAutoPayWithSource = (event: { target: { checked: any; }; }) => {
        if (event.target.checked) {
            setAutoPay(true);
            setUseDefaultCard(true);
        } else {
            setAutoPay(false);
        }
    };

    const handlePayWithDefaultCard = (event: { target: { checked: any; }; }) => {
        if (event.target.checked || autoPay) {
            setUseDefaultCard(true);
        } else {
            setUseDefaultCard(false);
        }
    };

    const handleSetAsDefaultCard = (event: { target: { checked: any; }; }) => {
        if (event.target.checked) {
            setAsDefaultCard(true);
        } else if (autoPay && !seller.isPaymentSourcePresent) {
            setAsDefaultCard(true);
        } else {
            setAsDefaultCard(false);
        }
    };

    const stripeKeyPublish =
        'pk_test_51Nf481Ae4RILjJWGS16n8CI5yhK3nWg0kTMZVvRTOgMOY4KBlgI21EcPsSj9tY4tfDTQWrlh1v0egnN0ozBT9ATQ00kuhJ8UrS'

    let paymentComponent;

    if (seller.isPaymentSourcePresent) {
        paymentComponent = <div style={{ display: 'flex' }}>
            <label>{JSON.stringify(getErrors)}</label>
            <label>
                <input onChange={handleIsAutoPayWithSource} type="checkbox" /> You want to be charged automatically
                and
                start a
                subscription?
                <div>
                    <input checked={getUseDefaultCard} onChange={handlePayWithDefaultCard} type="checkbox" /> You want to
                    pay

                    with a default card of this account?
                    {autoPay ? <div style={{ color: 'maroon' }}>Subscriptions *require* default cards for monthly
                        payments</div> : null}
                </div>
            </label>

            {!getUseDefaultCard ?
                <Stripe
                    stripeKey={stripeKeyPublish}
                    token={payNow}
                    email={seller.id.toString()}
                /> : <button style={{
                    color: "white",
                    height: "40px",
                    width: "100px",
                    backgroundColor: "cadetblue",
                    borderRadius: "5px",
                    border: "none"
                }} onClick={() => payNow('')}>pay with default card</button>
            }
        </div>
    } else if (!seller.isPaymentSourcePresent) {
        paymentComponent = <div>
            <div style={{ display: 'flex' }}>
                <label>{JSON.stringify(getErrors)}</label>
                <label>
                    <input checked={getAsDefaultCard} onChange={handleSetAsDefaultCard} type="checkbox" /> You want to make this a default card for this account?
                    {autoPay ? <div style={{ color: 'maroon' }}>Subscriptions *require* default cards for monthly
                        payments</div> : null}
                </label>
                <label>
                    <input onChange={handleIsAutoPay} type="checkbox" /> You want to be charged automatically and start a
                    subscription?
                </label>
            </div>
            <Stripe
                stripeKey={stripeKeyPublish}
                token={payNow}
                email={seller.id.toString()}
                description={"Buy AutoRia premium"}
            />
        </div>
    } else {
        paymentComponent = <div>Could not extract payment method...</div>
    }

    return (
        <div>
            <div style={{
                backgroundColor: "whitesmoke",
                fontSize: "9px",
                padding: "20px",
                borderRadius: "5px",
                columnGap: "10px"
            }}>
                <h4 style={{ color: "green" }}>Buy premium</h4>
                <h3 style={{ color: "green" }}>MARK: A Subscription lasts 1 month,
                    automatic subscriptions renew every month automatically by withdrawing money from your default
                    card</h3>
                {paymentComponent}
            </div>
            <hr />
            <div style={{
                backgroundColor: "whitesmoke",
                fontSize: "9px",
                padding: "20px",
                borderRadius: "5px",
                columnGap: "10px"
            }}>
                <h4 style={{ color: "green" }}>Add default payment source</h4>
                <Stripe
                    stripeKey={stripeKeyPublish}
                    token={addCard}
                    description={"Bind a card to AutoRia"}
                />
            </div>
        </div>
    );
};

export { StripeCheckout };

