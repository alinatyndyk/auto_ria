import React, { useState } from 'react';
import { CardElement, injectStripe } from 'react-stripe-elements';

const CheckoutForm = ({ stripe }) => {
    const [cardNumber, setCardNumber] = useState('');
    const [expDate, setExpDate] = useState('');
    const [cvc, setCVC] = useState('');
    const [rememberCard, setRememberCard] = useState(false);
    const [useDefaultCard, setUseDefaultCard] = useState(false);

    const handlePayment = async () => {
        // Use the cardNumber, expDate, and cvc values for making a payment using Stripe
        // You can also send the rememberCard and useDefaultCard values to your Spring Boot server
    };

    return (
        <form>
            <label>
                Card Number
                <CardElement
                    onChange={(e) => setCardNumber(e.complete ? e.value : '')}
                />
            </label>
            <label>
                Expiration Date
                <input
                    type="text"
                    value={expDate}
                    onChange={(e) => setExpDate(e.target.value)}
                />
            </label>
            <label>
                CVC
                <input
                    type="text"
                    value={cvc}
                    onChange={(e) => setCVC(e.target.value)}
                />
            </label>
            <label>
                Remember my card for this account
                <input
                    type="checkbox"
                    checked={rememberCard}
                    onChange={() => setRememberCard(!rememberCard)}
                />
            </label>
            <label>
                Just use my default card
                <input
                    type="checkbox"
                    checked={useDefaultCard}
                    onChange={() => setUseDefaultCard(!useDefaultCard)}
                />
            </label>
            <button onClick={handlePayment}>Pay</button>
        </form>
    );
};

export default injectStripe(CheckoutForm);