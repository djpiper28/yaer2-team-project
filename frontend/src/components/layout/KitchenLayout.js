import React, { useEffect, useState, useRef } from 'react';
import { Outlet } from 'react-router';
import { getStoredAccessToken } from '../api/TokenHandler';
import {
  NOTIFICATION_ACTIONS,
  useNotifications,
} from '../notifications/NotificationContextProvider';
import { nanoid } from 'nanoid';
import NotificationBox from '../notifications/NotificationBox';
import StaffNavBar from '../staff/StaffNavBar';

const KitchenLayout = () => {
  const ordersReference = useRef([]);

  useEffect(() => {
    webSockets();
  }, []);

  const { notificationDispatch } = useNotifications();

  const [orders, setOrders] = useState({
    'menu-items': [],
    'new-orders': [],
    'changed-orders': [],
    'removed-orders': [],
    notifications: [],
  });

  const webSockets = () => {
    getStoredAccessToken().then((r) => {
      const ws = new WebSocket(`wss://www.rhul-hack.com/rtos/?Authorization=${r}`);

      ws.addEventListener('open', (e) => {
        console.log(e);
      });

      ws.addEventListener('message', (e) => {
        const newData = JSON.parse(e.data);
        console.log(newData);

        //Initial orders
        if (!newData.hasOwnProperty('changed-orders')) {
          const ordersNewObject = { 'menu-items': [], 'new-orders': [], notifications: [] };

          if (newData.hasOwnProperty('notifications')) {
            ordersNewObject.notifications = newData.notifications;
          }

          ordersNewObject['menu-items'] = newData['menu-items'];
          ordersNewObject['new-orders'] = newData.orders;

          ordersNewObject['new-orders'].forEach((order) => {
            order.givenNew = false;
          });

          ordersNewObject['new-orders'].sort((a, b) => {
            return b['placed-time'] - a['placed-time'];
          });

          ordersReference.current = ordersNewObject;
          setOrders(ordersNewObject);
        } else {
          //Order updates

          const ordersNewObject = {};

          ordersNewObject['new-orders'] = [
            ...ordersReference.current['new-orders'],
            ...newData['new-orders'].slice(),
          ];

          if (newData.hasOwnProperty('new-notifs')) {
            newData['new-notifs'].forEach((not) => {
              not.givenNew = true;
            });

            ordersNewObject.notifications = [
              ...newData['new-notifs'],
              ...ordersReference.current['notifications'],
            ];
          }

          if (newData.hasOwnProperty('removed-notifs')) {
            ordersNewObject.notifications = ordersNewObject.notifications.filter(
              (not) => !newData['removed-notifs'].includes(not.id)
            );
          }

          ordersNewObject['menu-items'] = newData['menu-items'];
          ordersNewObject['changed-orders'] = newData['changed-orders'];
          ordersNewObject['removed-orders'] = newData['removed-orders'];

          ordersNewObject['new-orders'].forEach((order) => {
            order.givenNew = true;
          });

          ordersNewObject['new-orders'] = ordersNewObject['new-orders'];

          for (let order of newData['new-orders']) {
            notificationDispatch({
              type: NOTIFICATION_ACTIONS.ADD_NOTIFICATION,
              payload: {
                id: nanoid(),
                title: 'New Order',
                description: `Order ID: ${order['order-id'].substring(0, 4)}`,
                buttonText: 'Okay!',
                opacity: 'opacity-100',
              },
            });
          }

          ordersNewObject['new-orders'].sort((a, b) => {
            return b['placed-time'] - a['placed-time'];
          });

          if (newData.hasOwnProperty('removed-orders')) {
            ordersNewObject['new-orders'] = ordersNewObject['new-orders'].filter(
              (item) => !newData['removed-orders'].includes(item['order-id'])
            );
          }

          ordersReference.current = ordersNewObject;
          setOrders(ordersNewObject);
        }
      });

      ws.addEventListener('close', (e) => {
        console.log('Web sockets closed!');
        setTimeout(webSockets, 1000);
      });
    });
  };

  return (
    <div className="flex flex-col w-full h-full min-h-screen bg-snow-storm-300">
      <StaffNavBar />
      <Outlet context={[orders]} />
      <NotificationBox />
    </div>
  );
};
export default KitchenLayout;
