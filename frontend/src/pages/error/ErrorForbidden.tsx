import React, { FC } from 'react';
import './Forbidden.css'; // Импорт стилей

interface IProps {
    cause: string
}

const ErrorForbidden: FC<IProps> = ({cause}) => {
  return (
    <div className="forbidden-container">
      <h1 className="forbidden-title">403</h1>
      <p className="forbidden-message">Forbidden endpoint</p>
      <p className="forbidden-message">Cause: {cause}</p>
    </div>
  );
};

export default ErrorForbidden;