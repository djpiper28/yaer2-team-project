import React, { useState } from 'react';
import { Outlet } from 'react-router';
import OrderContextProvider from '../menu/OrderContextProvider';
import NotificationBox from '../notifications/NotificationBox';
import NavBar from '../ui/NavBar';
import SideMenu from '../ui/Sidemenu';

const DefaultLayout = () => {
  const [sidebar, setSidebar] = useState(false);

  const toggleSidebar = () => {
    setSidebar(!sidebar);
  };

  return (
    <OrderContextProvider>
      <div className="w-full h-screen flex flex-col overflow-hidden">
        <NavBar toggle={toggleSidebar} />
        <div
          className="flex h-full bg-fixed"
          onClick={() => {
            if (window.innerWidth < 640) {
              setSidebar(false);
            }
          }}>
          <div className="fixed w-full h-full -z-10 bg-snow-storm-300 flex" />
          <SideMenu open={sidebar} />
          <div
            className="w-full overflow-y-scroll transition-all scroll-smooth px-4"
            id="layout-parent">
            <div className="w-full mt-4 mb-16">
              <Outlet />
            </div>
          </div>
          <NotificationBox />
        </div>
      </div>
    </OrderContextProvider>
  );
};
export default DefaultLayout;
