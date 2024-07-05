import { FC, useEffect, useState } from 'react';
import { useParams } from "react-router";
import { ERole } from "../../../constants/role.enum";
import { useAppDispatch, useAppNavigate, useAppSelector } from "../../../hooks";
import { authActions } from "../../../redux/slices";

import '../logs/LoginForm.css';
import ErrorForbidden from '../../../pages/error/ErrorForbidden';

const ActivateForm: FC = () => {
    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();
    const { role } = useParams<{ role: string }>();

    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');

    const { activateSellerErrors } = useAppSelector(state => state.authReducer);

    const [activateError, setActivateError] = useState<string>();

    const activate = async () => {

        if (!code) {
            return <ErrorForbidden cause='Your activation link lacks a code' />
        }

            if (role === ERole.USER) {
                try {
                    await dispatch(authActions.activateSeller({ code })).unwrap();
                    navigate('/profile');
                } catch (err) {}
            }
    }

    return (
        <div style={{ display: "flex", justifyContent: "center", margin: "20px" }}>
            <div className="login-form">
                <div className="form-header">Activate my account</div>
                {activateSellerErrors ? <div className="error-message">{activateSellerErrors?.message}</div> : null}
                <button onClick={() => activate()} className="submit-button">Activate my account</button>
            </div>
        </div>
    );
};

export { ActivateForm };


