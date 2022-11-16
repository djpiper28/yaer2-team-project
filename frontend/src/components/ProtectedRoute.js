import React, { useEffect, useState } from 'react';
import { Navigate, Outlet } from 'react-router';
import { getStoredAccessToken } from './api/TokenHandler';

const ProtectedRoute = ({ children }) => {
  const [loggedIn, setLoggedIn] = useState({ loggedIn: 'dk' });
  useEffect(() => {
    getStoredAccessToken()
      .then((r) => {
        setLoggedIn({ loggedIn: 'true' });
      })
      .catch((r) => setLoggedIn({ loggedIn: 'false' }));
  }, []);

  console.log(loggedIn);
  if (loggedIn.loggedIn == 'true') {
    return <Outlet />;
  } else if (loggedIn.loggedIn == 'false') {
    return <Navigate to="/auth/login" />;
  } else {
    return null;
  }
};
export default ProtectedRoute;
