import { FC } from 'react';
import { FindCarById } from '../forms/car/FindCarById';
import './CarPanel.css';
import { CarForm } from './cars';

const CodePanel: FC = () => {
    return (
        <div className="car-panel">
            <h2>Car Tools</h2>
            <div className="forms-container">
                <div className="form-section">
                    <h3>Find Car by ID</h3>
                    <p>Search for a car in the database by its unique ID.</p>
                    <FindCarById />
                </div>
                <CarForm />
            </div>
        </div>
    );
};

export { CodePanel };

