// @flow
import * as React from 'react';
import {Component, useEffect} from "react";
import {carActions} from "../../redux/slices/car.slice";
import {carsService} from "../../services/cars.service";
import Car from "./Car";

class Cars extends React.Component<any, any>{
    constructor(props: any) {
        super(props);
        this.state = {
            cars: []
        }
    }

    componentDidMount() {
        const page = {page: 3};
        carsService.getAll().then(value => value.data)
            .then(value => this.setState({cars: value}))
    }

    render() {
        return(
            <div>
                <div>Cars</div>
                <div>{JSON.stringify(this.state.cars)}</div>
                {this.state.cars.map((car: { id: React.Key | null | undefined; }) => <Car key={car.id} car={car}/>)}
            </div>
        );
    }
}

export default Cars;