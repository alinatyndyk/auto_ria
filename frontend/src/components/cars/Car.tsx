// @flow
import * as React from 'react';
import {carsService} from "../../services/cars.service";

class Car extends React.Component<any, any>{
    constructor(props: any) {
        super(props);
    }
    render() {

        const {id, brand, price, currency} = this.props.car;

        return(
            <div>
                <div>Cars</div>
                <div>id: {id}</div>
                <div>brand: {brand}</div>
                <div>price: {price}</div>
                <div>currency: {currency}</div>
            </div>
        );
    }
}

export default Car;