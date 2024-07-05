import { FC, useEffect, useState } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { useAppDispatch, useAppNavigate, useAppSelector } from '../../hooks';
import { carActions } from '../../redux/slices';

import './FindCarById.css';

interface IFindCarById {
    id: number;
}

const FindCarById: FC = () => {
    const { reset, handleSubmit, register } = useForm<IFindCarById>();
    const navigate = useAppNavigate();
    const dispatch = useAppDispatch();
    const { errorGetById } = useAppSelector(state => state.carReducer);
    const [isHovered, setIsHovered] = useState(false);

    const find: SubmitHandler<IFindCarById> = async (body: IFindCarById) => {
        try {
            await dispatch(carActions.getById(body.id)).unwrap();
            navigate(`/cars/${body.id}`);
        } catch (err) {
            reset();
        }
    };

    useEffect(() => {
        const handleMouseEnter = () => {
            setIsHovered(true);
        };

        const handleMouseLeave = () => {
            setIsHovered(false);
        };

        const formElement = document.querySelector('.find-car-by-id .form-container');
        formElement?.addEventListener('mouseenter', handleMouseEnter);
        formElement?.addEventListener('mouseleave', handleMouseLeave);

        return () => {
            formElement?.removeEventListener('mouseenter', handleMouseEnter);
            formElement?.removeEventListener('mouseleave', handleMouseLeave);
        };
    }, []);

    return (
        <div className="find-car-by-id">
            <div>Find car by ID</div>
            {errorGetById && <div className="errorMessage">{errorGetById?.message}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(find)}>
                <div className="form-container">
                    <input type="number" placeholder="ID" {...register('id')} />
                    {isHovered && <div className="hover-text">Enables the search of banned cars*</div>}
                </div>
                <button type="submit">Go to car</button>
            </form>
        </div>
    );
};

export { FindCarById };


