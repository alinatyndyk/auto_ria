import React, {FC, useEffect, useState} from 'react';
import {useAppDispatch, useAppNavigate, useAppSelector} from "../../../hooks";
import {authActions} from "../../../redux/slices";
import {useParams} from "react-router";
import {ERole} from "../../../constants/role.enum";

const ActivateForm: FC = () => {
    const dispatch = useAppDispatch();
    const {errors} = useAppSelector(state => state.authReducer);
    const navigate = useAppNavigate();
    const {role} = useParams<{ role: string }>();

    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');

    const [getResponse, setResponse] = useState('');

    useEffect(() => {
        if (code == undefined) {
            setResponse("Error. Activation_Code_Absent");
        }
    }, [params])

    const activate = async () => {

        let payload;

        if (role == ERole.SELLER) {
            const res = await dispatch(authActions.activateSeller({code: code ?? ''}));
            payload = res.payload;
            setResponse(String(payload));
        } else if (role == ERole.CUSTOMER) {
            const res = await dispatch(authActions.activateCustomer({code: code ?? ''}));
            payload = res.payload;
            setResponse(String(payload));
        } else {
            setResponse(`Invalid_Role_Url: ${role}.`);
        }

        navigate('/profile')
    }
    return (
        <div>
            <div>
                <button onClick={() => navigate('/cars')}>Cars</button>
            </div>
            Activate my account
            {errors ? <div>{errors?.message}</div> : <div>{getResponse}</div>}
            <button onClick={() => activate()}>Activate my account</button>
        </div>
    );
};

export {ActivateForm};