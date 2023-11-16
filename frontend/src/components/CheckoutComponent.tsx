import StripeCheckout from 'react-stripe-checkout';
import axios from "axios";

export default function CheckoutComponent() {

    const payNow = async (token: any) => {
        console.log("yyyyyyyyyyyyy");
        console.log(token);
        console.log("xxxxxxxxxxxxxxxxxxx");
        try {
            const response =
                // await axios.post("http://localhost:8080/cars/buy-premium", {
                await axios.post("http://localhost:8080/cars/add-payment-source", {
                    amount: 1000,
                    id: "2",
                    token: token.id
                }, {
                    // headers: {
                    //     'Content-type': 'multipart/form-data'
                    // }
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

    const stripeKeyPublish =
        'pk_test_51Nf481Ae4RILjJWGS16n8CI5yhK3nWg0kTMZVvRTOgMOY4KBlgI21EcPsSj9tY4tfDTQWrlh1v0egnN0ozBT9ATQ00kuhJ8UrS'

    return (
        <div className={'checkout-form-insides'}>
            <h2>Checkout Form</h2>
            <div>
                <input type={"checkbox"} name={"Would you like to make this your account's card?"}/>
                <StripeCheckout
                    stripeKey={stripeKeyPublish}
                    label='Pay now'
                    name='Pay with credit card'
                    billingAddress
                    shippingAddress
                    amount={9000}
                    description={`Your total is...`}
                    token={payNow}
                />
            </div>
        </div>
    )
}