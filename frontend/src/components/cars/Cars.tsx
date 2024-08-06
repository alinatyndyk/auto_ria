import { FC, useEffect, useState } from 'react';
import { useSearchParams } from "react-router-dom";
import { useAppDispatch, useAppSelector } from "../../hooks";
import { CarsResponse } from '../../interfaces';
import { carActions } from "../../redux/slices";
import { Car } from "./Car";
import './Cars.css';
import ErrorForbidden from '../../pages/error/ErrorForbidden';

interface IProps {
    sellerId: number | null
}

const Cars: FC<IProps> = ({ sellerId }) => {
    const { cars, pagesInTotal, carAdded, numberOfElements } = useAppSelector(state => state.carReducer);
    const dispatch = useAppDispatch();
    const [searchParams, setSearchParams] = useSearchParams();

    const [getButtons, setButtons] = useState(true);
    const [getNextButtons, setNextButtons] = useState(false);
    const initialPage = searchParams.get("page");
    const [getPage, setPage] = useState<number>(initialPage ? parseInt(initialPage) : 1);
    let [gerCars, setCars] = useState<CarsResponse[]>([]);
    const [showError, setShowError] = useState(false);

    useEffect(() => {
        if (getPage > pagesInTotal && pagesInTotal > 0) {
            setShowError(true);
        } else {
            setShowError(false);
            searchParams.set('page', getPage.toString());
            setSearchParams(searchParams);
            if (sellerId != null) {
                dispatch(carActions.getBySeller({ page: getPage - 1, id: sellerId }));
            } else {
                dispatch(carActions.getAll(getPage));
            }
        }
    }, [getPage, sellerId, pagesInTotal]);

    useEffect(() => {
        if (!showError) {
            setCars(cars);
        }
    }, [cars, showError]);

    useEffect(() => {
        if (carAdded && numberOfElements < 2) {
            setCars(prevState => [...prevState, carAdded]);
        }
    }, [carAdded]);

    useEffect(() => {
        setButtons(getPage <= 1);
        setNextButtons(getPage >= pagesInTotal);
    }, [getPage, pagesInTotal]);

    const prevPage = () => {
        if (getPage > 1) setPage(prevPage => prevPage - 1);
    };

    const nextPage = () => {
        if (getPage < pagesInTotal) setPage(nextPage => nextPage + 1);
    };

    if (showError) {
        return <ErrorForbidden cause='Page doesnt exist' />;
    }

    return (
        <div className="cars-container">
            <h2 className="cars-title">Cars</h2>
            {pagesInTotal === 0 && <div className="no-cars-message">There are no cars to view</div>}
            {gerCars.map(car => <Car key={car.id} car={car} />)}
            <div className="pagination">
                <button disabled={getButtons} onClick={prevPage}>Prev</button>
                <div className="pagination-info">Page {getPage} of {pagesInTotal}</div>
                <button disabled={getNextButtons} onClick={nextPage}>Next</button>
            </div>
        </div>
    );
};

export { Cars };
