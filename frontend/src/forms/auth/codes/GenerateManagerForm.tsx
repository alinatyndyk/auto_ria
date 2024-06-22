import React, {FC, useState} from 'react';
import {SubmitHandler, useForm} from "react-hook-form";
import {useAppDispatch, useAppSelector} from "../../../hooks";
import {authActions} from "../../../redux/slices";
import {IGenerateCode} from "../../../interfaces";

const GenerateManagerForm: FC = () => {
    const {reset, handleSubmit, register} = useForm<IGenerateCode>();
    const dispatch = useAppDispatch();
    const {generateManagerErrors} = useAppSelector(state => state.authReducer);

    const [getResponse, setResponse] = useState('');

    const activate: SubmitHandler<IGenerateCode> = async (email: IGenerateCode) => {

        const {payload} = await dispatch(authActions.generateManager(email));

        setResponse(String(payload));

        reset();
    }
    return (
        <div>
            Generate code for manager
            {generateManagerErrors ? <div>{generateManagerErrors?.message}</div> : <div>{getResponse}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(activate)}>
                <div>
                    <input type="text" placeholder={'email'} {...register('email')}/>
                </div>
                <button>generate a new manager</button>
            </form>
        </div>
    );
};

export {GenerateManagerForm};