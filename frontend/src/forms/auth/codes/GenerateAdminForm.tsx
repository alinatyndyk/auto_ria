import { FC, useState } from 'react';
import { SubmitHandler, useForm } from "react-hook-form";
import { useAppDispatch, useAppSelector } from "../../../hooks";
import { IGenerateCode } from "../../../interfaces";
import { authActions } from "../../../redux/slices";

const GenerateAdminForm: FC = () => {
    const { reset, handleSubmit, register } = useForm<IGenerateCode>();
    const dispatch = useAppDispatch();
    const { generateAdminErrors } = useAppSelector(state => state.authReducer);

    const [getResponse, setResponse] = useState('');

    const activate: SubmitHandler<IGenerateCode> = async (email: IGenerateCode) => {

        const { payload } = await dispatch(authActions.generateAdmin(email));

        setResponse(String(payload));

        reset();
    }
    return (
        <div>
            Generate code for admin
            {generateAdminErrors ? <div>{generateAdminErrors?.message}</div> : <div>{getResponse}</div>}
            <form encType="multipart/form-data" onSubmit={handleSubmit(activate)}>
                <div>
                    <input type="text" placeholder={'email'} {...register('email')} />
                </div>
                <button>generate a new admin</button>
            </form>
        </div>
    );
};

export { GenerateAdminForm };
