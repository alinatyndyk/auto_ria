import { FC } from 'react';
import { GenerateManagerForm } from '../../forms/auth/codes/GenerateManagerForm';
import { GenerateAdminForm } from '../../forms/auth/codes/GenerateAdminForm';

const CarPanel: FC = () => {
    return (
        <div className="car-panel">
            <h2>Admin & Manager Tools</h2>
            <div className="forms-container">
                <div className="form-section">
                    <h3>Generate Manager Code</h3>
                    <p>Generate a new manager authorization code for new users.</p>
                    <GenerateManagerForm />
                </div>
                <div className="form-section">
                    <h3>Generate Admin Code</h3>
                    <p>Generate a new admin authorization code for administrative purposes.</p>
                    <GenerateAdminForm />
                </div>
            </div>
        </div>
    );
};

export { CarPanel };
