import { FC, useEffect, useState } from 'react';
import { useSearchParams } from "react-router-dom";
import { useAppDispatch, useAppSelector } from "../../hooks";
import { carActions } from "../../redux/slices";
import { Car } from "./Car";
import './Cars.css';

interface IProps {
    sellerId: number | null
}

const Cars: FC<IProps> = ({ sellerId }) => {
    const { cars, pagesInTotal } = useAppSelector(state => state.carReducer);
    const dispatch = useAppDispatch();
    const [searchParams, setSearchParams] = useSearchParams();

    const [getButtons, setButtons] = useState(true);
    const [getNextButtons, setNextButtons] = useState(false);
    let [getPage, setPage] = useState<number>(1);


    useEffect(() => {
        searchParams.set('page', getPage.toString());
        setSearchParams(searchParams);
        if (sellerId != null) {
            dispatch(carActions.getBySeller({ page: getPage - 1, id: sellerId }));
        } else {
            dispatch(carActions.getAll(getPage));
        }

        if (getPage <= 1) {
            setButtons(true);
        } else {
            setButtons(false);
        }

        if (getPage >= pagesInTotal) {
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
        <div className="cars-container">
            <h2 className="cars-title">Cars</h2>
            {pagesInTotal === 0 && <div className="no-cars-message">There are no cars to view</div>}
            {cars.map(car => <Car key={car.id} car={car} />)}
            <div className="pagination">
                <button disabled={getButtons} onClick={prevPage}>Prev</button>
                <div className="pagination-info">Page {getPage} of {pagesInTotal}</div>
                <button disabled={getNextButtons} onClick={nextPage}>Next</button>
            </div>
        </div>
    );
};

export { Cars };

