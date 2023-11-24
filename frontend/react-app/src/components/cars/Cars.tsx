import React, {FC, useEffect, useState} from 'react';
import {useAppDispatch, useAppSelector} from "../../hooks";
import {Car} from "./Car";
import {carActions} from "../../redux/slices";
import {set} from "react-hook-form";
import {CarForm} from "../../forms";

interface IProps {
    sellerId: number | null
}

const Cars: FC<IProps> = ({sellerId}) => {
    const {cars, trigger, pageCurrent, pagesInTotal} = useAppSelector(state => state.carReducer);
    const dispatch = useAppDispatch();

    const [getButtons, setButtons] = useState(true);
    const [getNextButtons, setNextButtons] = useState(false);
    let [getPage, setPage] = useState<number>(0);

    useEffect(() => {
        console.log("effect");
        console.log(pageCurrent, pagesInTotal)

        if (sellerId != null) {
            dispatch(carActions.getBySeller({page: getPage, id: sellerId}))
        } else {
            dispatch(carActions.getAll(getPage));
        }

        if (pageCurrent <= 0) {
            setButtons(true);
        } else {
            setButtons(false);
        }

        if (pageCurrent >= pagesInTotal) {
            setNextButtons(true);
        } else {
            setNextButtons(false);
        }

    }, [dispatch, getPage, sellerId])


    const prevPage = () => {
        setPage(prevState => prevState++);
    };

    const nextPage = () => {
        setPage(prevState => prevState--);
    };

    return (
        <div>
            Cars
            <CarForm/>
            {cars.map(car => <Car key={car.id} car={car}/>)}
            <div style={{display: 'flex'}}>
                <button disabled={getButtons} onClick={() => prevPage()}>prev</button>
                <button disabled={getNextButtons} onClick={() => nextPage()}>next</button>
                <div>total: {pagesInTotal}</div>
            </div>
        </div>
    );
};

export {Cars};