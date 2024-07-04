import React, { FC, useState } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '../../../hooks';
import { authActions } from '../../../redux/slices';
import { IError, IGenerateCode } from '../../../interfaces';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEnvelope } from '@fortawesome/free-solid-svg-icons';
import './GenerateForm.css'; // Import your CSS file for this form

const GenerateManagerForm: FC = () => {
    const { reset, handleSubmit, register } = useForm<IGenerateCode>();
    const dispatch = useAppDispatch();
    const { generateManagerErrors } = useAppSelector(state => state.authReducer);

    const [getResponse, setResponse] = useState<string>('');
    const [getEmail, setEmail] = useState<string>('');
    const [showButtons, setShowButtons] = useState<boolean>(true);

    const activate: SubmitHandler<IGenerateCode> = async (data: IGenerateCode) => {
        setEmail(data.email);
        const { payload } = await dispatch(authActions.generateManager(data));
        const message = String(payload);
        setResponse(message);
    };

    const handleYesClick = async () => {
        // Perform custom action when "Yes" is clicked
        const { payload, type } = await dispatch(authActions.toManager({ email: getEmail }));
        const lastWord = type.substring(type.lastIndexOf('/') + 1);

        if (lastWord === 'fulfilled') {
            const message = String(payload);
            setResponse(message);
        } else if (lastWord == 'rejected'){
            const err = payload as IError;
            setResponse(err?.message ?? 'Error setting role');
        }

        setShowButtons(false); // Hide buttons after action
    };

    const handleNoClick = () => {
        // Reset form and remove error message when "No" is clicked
        reset();
        setShowButtons(false);
        setResponse(''); // Clear response message
    };

    return (
        <div className="generate-form">
            <div className="form-header">
                <FontAwesomeIcon icon={faEnvelope} className="form-icon" />
                <span className="form-title"> Generate code for manager</span>
            </div>
            {generateManagerErrors ? (
                <div className="error-message">
                    {generateManagerErrors.message}
                    {showButtons && (generateManagerErrors?.message === 'User with this email already exists. Do you want to change their role?') ? (
                        // <div className="button-container">
                        <div>
                            <button onClick={handleYesClick}>Yes</button>
                            <button onClick={handleNoClick}>No</button>
                        </div>
                    ) : null}
                </div>
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
