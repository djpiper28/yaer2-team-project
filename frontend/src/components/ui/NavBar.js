import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { MenuIcon } from '@heroicons/react/solid';
import { getStoredAccessToken, logout } from '../api/TokenHandler';
import Button from './Button';
import { LoginIcon, LogoutIcon } from '@heroicons/react/solid';
import { useNavigate } from 'react-router';
import BasketIndicator from './BasketIndicator';

/**
 * Navigation bar displayed on the top of the page.
 * @param toggle - This toggles the sidebar on or off.
 * @author Lim
 * @return Component
 */
const NavBar = ({ toggle }) => {
  const [loggedIn, setLoggedIn] = useState(false);

  const navigation = useNavigate();
  useEffect(() => {
    getStoredAccessToken()
      .then((r) => {
        setLoggedIn(true);
      })
      .catch(() => setLoggedIn(false));
  }, []);

  const getLoggedInIndicator = () => {
    if (loggedIn) {
      return (
        <Button
          text="Logout"
          icon={LogoutIcon}
          className="rounded bg-orange-500 xs:flex  hidden"
          onClick={() => {
            logout();
            setLoggedIn(false);
            navigation('/');
          }}
        />
      );
    } else {
      return (
        <Button
          text="Login"
          icon={LoginIcon}
          className="rounded bg-orange-500 xs:flex hidden"
          onClick={() => navigation('/auth/login')}
        />
      );
    }
  };

  return (
    <div className="flex-shrink-0 h-16 bg-gray-900 flex flex-row justify-between items-center gap-2 border-b-[2px] border-snow-storm-300 shadow-lg">
      <div className="w-full h-full flex justify-start items-center">
        <div className="h-full flex items-center text-snow-storm-300 px-4">
          <Link to="#" className="text-5xl" onClick={() => toggle()}>
            <MenuIcon className="w-12 h-12" />
          </Link>
        </div>
        <div className="text-snow-storm-300">
          <Link to="/">
            <p className="font-bold sm:text-5xl text-3xl text-snow-storm-300 align-middle">Oxana</p>
          </Link>
        </div>
      </div>
      <div className="w-full h-full px-4 flex justify-end items-center gap-2">
        <BasketIndicator />
        {getLoggedInIndicator()}
      </div>
    </div>
  );
};
export default NavBar;
