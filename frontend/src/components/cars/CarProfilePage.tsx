import { FC } from 'react';
import { Cars } from './Cars';
import { CarForm } from '../../forms';
import { securityService } from '../../services/security.service';
import ErrorForbidden from '../../pages/error/ErrorForbidden';

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

