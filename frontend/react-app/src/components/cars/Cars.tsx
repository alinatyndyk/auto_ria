import React, {FC, useEffect} from 'react';
import {useAppDispatch, useAppSelector} from "../../hooks";
import {Car} from "./Car";
import {carActions} from "../../redux/slices";

const Cars: FC = () => {
    const {cars, trigger} = useAppSelector(state => state.carReducer);
    const dispatch = useAppDispatch();

    useEffect(() => {
        dispatch(carActions.getAll(0))
    }, [dispatch, trigger])

    return (
        <div>
            Cars
            {/*{JSON.stringify(cars)}*/}
            {cars.map(car => <Car key={car.id} car={car}/>)}
        </div>
    );
};

export {Cars};