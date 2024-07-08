import { FC } from 'react';
import './LoadingPage.css';

const LoadingPage: FC = () => {
    return (
        <div className="loading-page">
            <div className="spinner"></div>
            <div>Loading...</div>
        </div>
    );
};

export default LoadingPage;
