import React from 'react';
import { Outlet } from 'react-router';
import NotificationBox from '../notifications/NotificationBox';

const AuthLayout = () => {
  return (
    <div className="w-screen flex justify-center bg-snow-storm-300 px-4">
      <div className="max-w-screen-md w-screen h-screen">
        <Outlet />
      </div>
      <NotificationBox />
    </div>
  );
};
export default AuthLayout;
