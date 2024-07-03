import React, { FC, useState } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '../../../hooks';
import { authActions } from '../../../redux/slices';
import { IGenerateCode } from '../../../interfaces';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEnvelope } from '@fortawesome/free-solid-svg-icons';
import './GenerateForm.css'; // Import your CSS file for this form

const GenerateManagerForm: FC = () => {
    const { reset, handleSubmit, register } = useForm<IGenerateCode>();
    const dispatch = useAppDispatch();
    const { generateManagerErrors } = useAppSelector(state => state.authReducer);

    const [getResponse, setResponse] = useState<string>('');

    const activate: SubmitHandler<IGenerateCode> = async (data: IGenerateCode) => {
        const { payload } = await dispatch(authActions.generateManager(data));
        setResponse(String(payload));
        reset();
    };

    return (
        <div className="generate-form">
            <div className="form-header">
                <FontAwesomeIcon icon={faEnvelope} className="form-icon" />
                <span className="form-title"> Generate code for manager</span>
            </div>
            {generateManagerErrors ? (
                <div className="error-message">{generateManagerErrors.message}</div>
            ) : (
                <div className="success-message">{getResponse}</div>
            )}
            <form onSubmit={handleSubmit(activate)} className="form-container">
                <input
                    type="text"
                    placeholder="Enter email"
                    {...register('email', { required: true })}
                    className="form-input"
                />
                <button type="submit" className="form-button">
                    Generate a new manager
                </button>
            </form>
        </div>
    );
};

export { GenerateManagerForm };

