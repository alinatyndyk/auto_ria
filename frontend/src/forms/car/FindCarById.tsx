import React, { FC, useState } from 'react';
import { SubmitHandler, useForm } from "react-hook-form";
import { useAppDispatch, useAppNavigate, useAppSelector } from '../../hooks';
import { sellerActions } from '../../redux/slices/seller.slice';

interface IFindCarById {
    id: number
}

const FindCarById: FC = () => {
    const { reset, handleSubmit, register } = useForm<IFindCarById>();
    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();
    const {errors} = useAppSelector(state => state.sellerReducer);

    const [getResponse, setResponse] = useState('');

    const find: SubmitHandler<IFindCarById> = async (body: IFindCarById) => {

        // const { payload } = await dispatch(sellerActions.getById(body.id));

        // setResponse(String(payload));

        navigate(`/cars/${body.id}`);

        reset();
    }
    return (
        <div>
            find car by id
            {errors ? <div>{errors?.message}</div> : <div>{getResponse}</div>}
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