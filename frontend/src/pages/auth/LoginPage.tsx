import { FC } from 'react';
import LoginForm from '../../forms/auth/logs/LoginForm';
import { useAppSelector } from '../../hooks';
import ErrorForbidden from '../error/ErrorForbidden';

const LoginPage: FC = () => {

    const { isAuth } = useAppSelector(state => state.authReducer);

    if (isAuth) {
        return <ErrorForbidden cause='You are already logged in' />
    }

    return (
        <div style={{ display: 'flex', justifyContent: 'center', margin: '20px' }}>
            <LoginForm />
        </div>
    );
};

export { LoginPage };

