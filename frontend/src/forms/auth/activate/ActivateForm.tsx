import { FC, useEffect, useState } from 'react';
import { useParams } from "react-router";
import { ERole } from "../../../constants/role.enum";
import { useAppDispatch, useAppNavigate, useAppSelector } from "../../../hooks";
import { authActions } from "../../../redux/slices";

const ActivateForm: FC = () => {
    const dispatch = useAppDispatch();
    const navigate = useAppNavigate();
    const { role } = useParams<{ role: string }>();

    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');

    const { activateSellerErrors } = useAppSelector(state => state.authReducer);

    const activate = () => {
        if (role === ERole.USER) {
            dispatch(authActions.activateSeller({ code: code ?? '' }));
        }

        if (activateSellerErrors === null) {
            navigate('/profile');
        }
    }
    return (
        <div>
            <div>
                <button onClick={() => navigate('/cars')}>Cars</button>
            </div>
            Activate my account
            {activateSellerErrors ? <div>{activateSellerErrors?.message}</div> : null}
            <button onClick={() => activate()}>Activate my account</button>
        </div>
    );
};

export { ActivateForm };

