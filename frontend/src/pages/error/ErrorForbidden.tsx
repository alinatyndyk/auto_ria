import React, { FC } from 'react';
import './Forbidden.css';
import { useLocation } from 'react-router-dom';

interface IProps {
    cause: string
}

const ErrorForbidden: FC<IProps> = ({ cause }) => {
    const location = useLocation();
    const causeState = location.state?.cause || 'Unknown error occurred';
    return (
        <div className="forbidden-container">
            <h1 className="forbidden-title">403</h1>
            <p className="forbidden-message">Forbidden endpoint</p>
            <p className="forbidden-message">Cause:  {cause === 'Forbidden access.' && causeState !== null ? causeState : cause}</p>
        </div>
    );
};

export default ErrorForbidden;