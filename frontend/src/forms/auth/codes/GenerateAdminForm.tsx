import { faEnvelope } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { FC, useState } from 'react';
import { SubmitHandler, useForm } from "react-hook-form";
import { useAppDispatch, useAppSelector } from "../../../hooks";
import { IGenerateCode } from "../../../interfaces";
import { authActions } from "../../../redux/slices";
import './GenerateForm.css';

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
        <div className="generate-form">
            <div className="form-header">
                <FontAwesomeIcon icon={faEnvelope} className="form-icon" />
                <span className="form-title"> Generate code for admin</span>
            </div>
            {generateAdminErrors ? <div className="error-message">{generateAdminErrors.message}</div> : <div>{getResponse}</div>}
            <form onSubmit={handleSubmit(activate)}>
                <div>
                    <input type="text" placeholder="Email" {...register('email')} />
                </div>
                <button type="submit">Generate a New Admin</button>
            </form>
        </div>
    );
};

export { GenerateAdminForm };
