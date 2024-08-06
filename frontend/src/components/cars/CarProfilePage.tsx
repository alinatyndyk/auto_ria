import { FC } from 'react';
import { CarForm } from '../../forms';
import ErrorForbidden from '../../pages/error/ErrorForbidden';
import { securityService } from '../../services/security.service';
import { Cars } from './Cars';

const CarProfilePage: FC = () => {
  const AuthObj = localStorage.getItem('authorization');
  if (AuthObj !== null) {
    const auth = securityService.decryptObject(AuthObj);
    if (auth?.id) {
      return (
        <div style={{display: 'flex'}}>
          <Cars sellerId={auth.id} />
          <CarForm />
        </div>
      );
    }
  }
  return <ErrorForbidden cause="Login to access this page" />;
};

export default CarProfilePage;

