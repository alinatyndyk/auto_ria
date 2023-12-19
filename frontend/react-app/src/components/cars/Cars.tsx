import React, {FC, useEffect, useState} from 'react';
import {useAppDispatch, useAppSelector} from "../../hooks";
import {Car} from "./Car";
import {carActions} from "../../redux/slices";
import {useSearchParams} from "react-router-dom";

interface IProps {
    sellerId: number | null
}

export interface IMessage {
    id: number,
    content: string,
    senderId: number,
    receiverId: number,
    chatId: number,
    isEdited: boolean | null,
    updatedAt: number[],
    createdAt: number[]
}

const Cars: FC<IProps> = ({sellerId}) => {
    const {cars, pagesInTotal} = useAppSelector(state => state.carReducer);
    const dispatch = useAppDispatch();
    const [searchParams, setSearchParams] = useSearchParams();

    const [getButtons, setButtons] = useState(true);
    const [getNextButtons, setNextButtons] = useState(false);
    let [getPage, setPage] = useState<number>(0);

    useEffect(() => {

        searchParams.set('page', getPage.toString());
        setSearchParams(searchParams);

        if (sellerId != null) {
            dispatch(carActions.getBySeller({page: getPage, id: sellerId})).then(() => {
            });
        } else {
            dispatch(carActions.getAll(getPage));
        }

        if (getPage <= 0) {
            setButtons(true);
        } else {
            setButtons(false);
        }

        if (getPage + 1 >= pagesInTotal) {
            setNextButtons(true);
        } else {
            setNextButtons(false);
        }

    }, [getPage, sellerId, pagesInTotal])


    const prevPage = () => {
        setPage(prevState => prevState - 1);
    };

    const nextPage = () => {
        setPage(prevState => prevState + 1);
    };

    return (
        <div>
            Cars
            {pagesInTotal == 0 && <div style={{color: "blue"}}>There are no cars to view</div>}
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