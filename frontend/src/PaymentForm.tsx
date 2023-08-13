import {CardElement, useStripe, useElements} from '@stripe/react-stripe-js';

const PaymentForm = () => {
    const stripe = useStripe();
    const elements = useElements();

    const payNow = async (event: { preventDefault: () => void; }) => {
        event.preventDefault();
        if (!stripe || !elements) {
            // Stripe.js has not yet loaded.
            // Make sure to disable form submission until Stripe.js has loaded.
            return;
        }



        // Handle payment submission
    };

    return (
        <form onSubmit={payNow}>
            <CardElement/>
            <button type="submit" disabled={!stripe}>
                Pay
            </button>
        </form>
    );
};

export default PaymentForm;
