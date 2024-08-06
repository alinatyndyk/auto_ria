import { faEnvelope } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { FC, useState } from 'react';
import { SubmitHandler, useForm } from 'react-hook-form';
import { useAppDispatch, useAppSelector } from '../../../hooks';
import { IGenerateCode } from '../../../interfaces';
import { authActions } from '../../../redux/slices';
import './GenerateForm.css';

const GenerateManagerForm: FC = () => {
    const { reset, handleSubmit, register } = useForm<IGenerateCode>();
    const dispatch = useAppDispatch();
    const { generateManagerErrors } = useAppSelector(state => state.authReducer);

    const [getResponse, setResponse] = useState<string>('');

    const activate: SubmitHandler<IGenerateCode> = async (data: IGenerateCode) => {
        const { payload } = await dispatch(authActions.generateManager(data));
        const message = String(payload);
        setResponse(message);
        reset();
    };

    return (
        <div className="generate-form">
            <div className="form-header">
                <FontAwesomeIcon icon={faEnvelope} className="form-icon" />
                <span className="form-title">Generate Manager Code</span>
            </div>
            {generateManagerErrors ? (
                <div className="message error-message">
                    {generateManagerErrors.message}
                </div>
            ) : (
                getResponse && <div className="message success-message">{getResponse}</div>
            )}
            <form onSubmit={handleSubmit(activate)} className="form-container">
                <input
                    type="email"
                    placeholder="Enter manager's email"
                    {...register('email', { required: true })}
                    className="form-input"
                />
                <button type="submit" className="form-button">
                    Generate Code
                </button>
            </form>
        </div>
    );
};

export { GenerateManagerForm };
