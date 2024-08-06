import axios from "axios";
import { useState } from 'react';
import Stripe from "react-stripe-checkout";
import { useAppDispatch } from "../../hooks";
import { IError } from "../../interfaces";
import { sellerActions } from "../../redux/slices/seller.slice";
import { authService } from "../../services";
import useTheme from "../hooks/useTheme";
import styles from './StripeCheckout.module.css';

const StripeCheckout = () => {
    const seller = useTheme();
    const dispatch = useAppDispatch();

    const [getUseDefaultCard, setUseDefaultCard] = useState(false);
    const [getAsDefaultCard, setAsDefaultCard] = useState(false);
    const [autoPay, setAutoPay] = useState(false);
    const [getErrors, setErrors] = useState<String | null>(null);

    const payNow = async (token: any) => {
        try {
            const response = await axios.post("http://localhost:8080/payments/buy-premium", {
                token: token.id,
                useDefaultCard: getUseDefaultCard,
                setAsDefaultCard: getAsDefaultCard,
                autoPay: autoPay
            }, {
                headers: {
                    Authorization: `Bearer ${authService.getAccessToken()}`
                }
            });

            if (response.status === 200) {
                dispatch(sellerActions.toggle());
            }
        } catch (e) {
            const error = e as { response: { data: IError } };
            setErrors(String(error.response.data.message));
        }
    };

    const addCard = async (token: any) => {
        try {
            const response = await axios.post("http://localhost:8080/payments/add-payment-source", {
                token: token.id
            }, {
                headers: {
                    Authorization: `Bearer ${authService.getAccessToken()}`
                }
            });

        } catch (e) {
            const error = e as { response: { data: IError } };
            setErrors(String(error.response.data.message));
        }
    };

    const handleIsAutoPay = (event: { target: { checked: boolean; }; }) => {
        setAutoPay(event.target.checked);
        if (event.target.checked) {
            setAsDefaultCard(true);
        }
    };

    const handleIsAutoPayWithSource = (event: { target: { checked: boolean; }; }) => {
        setAutoPay(event.target.checked);
        if (event.target.checked) {
            setUseDefaultCard(true);
        }
    };

    const handlePayWithDefaultCard = (event: { target: { checked: boolean; }; }) => {
        setUseDefaultCard(event.target.checked || autoPay);
    };

    const handleSetAsDefaultCard = (event: { target: { checked: boolean; }; }) => {
        setAsDefaultCard(event.target.checked || (autoPay && !seller.paymentSourcePresent));
    };

    const stripeKeyPublish = 'pk_test_51Nf481Ae4RILjJWGS16n8CI5yhK3nWg0kTMZVvRTOgMOY4KBlgI21EcPsSj9tY4tfDTQWrlh1v0egnN0ozBT9ATQ00kuhJ8UrS';

    let paymentComponent;
    const isPaymentSourcePresent = seller.paymentSourcePresent ?? false;

    if (isPaymentSourcePresent) {
        paymentComponent = (
            <div className={styles.paymentContainer}>
                <label>
                    <input onChange={handleIsAutoPayWithSource} type="checkbox" /> You want to be charged automatically and start a subscription?
                    <div>
                        <input checked={getUseDefaultCard} onChange={handlePayWithDefaultCard} type="checkbox" /> You want to pay with a default card of this account?
                        {autoPay ? <div className={styles.errorMessage}>Subscriptions *require* default cards for monthly payments</div> : null}
                    </div>
                    {getErrors && <div className={styles.errorMessage}>{getErrors}</div>}
                </label>
                {!getUseDefaultCard ? (
                    <Stripe
                        stripeKey={stripeKeyPublish}
                        token={payNow}
                        email={seller.id ? seller.id.toString() : ''}
                    />
                ) : (
                    <button className={styles.stripeCheckoutButton} onClick={() => payNow({ id: '' })}>pay with default card</button>
                )}
            </div>
        );
    } else {
        paymentComponent = (
            <div>
                <div className={styles.paymentContainer}>
                    {getErrors && <div className={styles.errorMessage}>{JSON.stringify(getErrors)}</div>}
                    <label>
                        <input checked={getAsDefaultCard} onChange={handleSetAsDefaultCard} type="checkbox" /> You want to make this a default card for this account?
                        {autoPay ? <div className={styles.errorMessage}>Subscriptions *require* default cards for monthly payments</div> : null}
                    </label>
                    <label>
                        <input onChange={handleIsAutoPay} type="checkbox" /> You want to be charged automatically and start a subscription?
                    </label>
                </div>
                <Stripe
                    stripeKey={stripeKeyPublish}
                    token={payNow}
                    email={seller.id ? seller.id.toString() : ''}
                    description={"Buy AutoRia premium"}
                />
            </div>
        );
    }

    return (
        <div>
            <div className={styles.stripeCheckoutContainer}>
                <h4>Buy premium</h4>
                <h3>MARK: A Subscription lasts 1 month, automatic subscriptions renew every month automatically by withdrawing money from your default card</h3>
                {paymentComponent}
            </div>
            <hr />
            <div className={styles.stripeCheckoutContainer}>
                <h4>Add default payment source</h4>
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

