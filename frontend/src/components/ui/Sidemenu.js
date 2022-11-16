import React, { useEffect, useState } from 'react';
import { getStoredAccessToken } from '../api/TokenHandler';
import { useNavigate } from 'react-router';
import { Link } from 'react-router-dom';
import { SidebarPage } from './SidebarPage';
import { LoginIcon, LogoutIcon } from '@heroicons/react/solid';
import Button from './Button';
import clsx from 'clsx';

const SideMenu = ({ open }) => {
  const [loggedIn, setLoggedIn] = useState(false);

  const navigation = useNavigate();
  useEffect(() => {
    getStoredAccessToken()
      .then((r) => {
        setLoggedIn(true);
      })
      .catch(() => setLoggedIn(false));
  }, []);

  const getWidth = () => {
    if (open) {
      return 'w-72';
    }
    return 'w-0 opacity-0';
  };

  const getLoggedInIndicator = () => {
    if (loggedIn) {
      return (
        <Button
          text="Logout"
          icon={LogoutIcon}
          className="rounded bg-orange-500"
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
          className="rounded bg-orange-500"
          onClick={() => navigation('/auth/login')}
        />
      );
    }
  };
  return (
    <div
      className={clsx(
        'w-full h-screen bg-gray-800 transition-all z-50 sm:static absolute',
        getWidth()
      )}>
      <ul className="w-full">
        {SidebarPage.map((item) => {
          return (
            <li className="flex justify-start items-center py-2 pr-4 list-none h-16 hover:bg-gray-700 hover:rounded">
              <Link to={item.path} className={item.cName}>
                {item.icon}
                <span className="ml-2.5 mt-2.5">{item.title}</span>
              </Link>
            </li>
          );
        })}
        <div className="w-full flex justify-center my-2">{getLoggedInIndicator()}</div>
      </ul>
    </div>
  );
};
export default SideMenu;
