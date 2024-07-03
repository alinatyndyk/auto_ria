import { FC } from 'react';
import { SubmitHandler, useForm } from "react-hook-form";
import { useAppDispatch, useAppNavigate, useAppSelector } from '../../hooks';
import { carActions } from '../../redux/slices';

import './FindCarById.css'; // Импортируем CSS для стилей

interface IFindCarById {
    id: number;
}

const FindCarById: FC = () => {
    const { reset, handleSubmit, register } = useForm<IFindCarById>();
    const navigate = useAppNavigate();
    const dispatch = useAppDispatch();
    const { errorGetById } = useAppSelector(state => state.carReducer);

    const find: SubmitHandler<IFindCarById> = async (body: IFindCarById) => {
        await dispatch(carActions.getById(body.id));
        if (errorGetById === null) {
            navigate(`/cars/${body.id}`);
        }
        reset();
    }

    return (
        <div className="find-car-by-id">
            <div>Find car by ID</div>
            {errorGetById && <div className="errorMessage">{errorGetById?.message}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(find)}>
                <div>
                    <input type="number" placeholder="ID" {...register('id')} />
                </div>
                <button type="submit">Go to car</button>
            </form>
        </div>
    );
};

export { FindCarById };


