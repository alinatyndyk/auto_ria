import { FC } from 'react';
import { SubmitHandler, useForm } from "react-hook-form";
import { useAppDispatch, useAppNavigate, useAppSelector } from '../../hooks';
import { sellerActions } from '../../redux/slices/seller.slice';
import { carActions } from '../../redux/slices';

interface IFindCarById {
    id: number
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
        <div>
            find car by id
            {errorGetById && <div>{errorGetById?.message}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(find)}>
                <div>
                    <input type="number" placeholder={'id'} {...register('id')} />
                </div>
                <button>to car</button>
            </form>
        </div>
    );
};

export { FindCarById };

