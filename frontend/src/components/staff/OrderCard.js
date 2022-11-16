import clsx from 'clsx';
import React, { useState, useEffect } from 'react';
import Button from '../ui/Button';
import { CheckCircleIcon } from '@heroicons/react/outline';
import { postChangeOrderToComplete } from '../api/ApiClient';
import {
  NOTIFICATION_ACTIONS,
  useNotifications,
} from '../notifications/NotificationContextProvider';
import { nanoid } from 'nanoid';

/**
 * OrderCard to display incoming orders to both customers and staff.
 */

const OrderCard = ({
  orderid,
  givenNew,
  tableNumber,
  items,
  placedTime,
  getItemNameFromId,
  status,
}) => {
  let counter = 0;

  const { notifications, notificationDispatch } = useNotifications();
  const [newOrderStyles, setNewOrderStyles] = useState(
    givenNew ? 'border-2 border-orange-500' : ''
  );

  const [currentStatus, setCurrentStatus] = useState(status);

  useEffect(() => {
    if (givenNew) {
      setTimeout(() => {
        setNewOrderStyles('');
      }, 2000);
    }
  }, []);

  const markComplete = () => {
    postChangeOrderToComplete(orderid, currentStatus + 1).then((r) => {
      notificationDispatch({
        type: NOTIFICATION_ACTIONS.ADD_NOTIFICATION,
        payload: {
          id: nanoid(),
          title: `${orderid} complete!`,
          description: ``,
          buttonText: 'Okay',
          opacity: 'opacity-100',
        },
      });
    });
    if (currentStatus == 0) {
      setCurrentStatus(1);
    }
  };

  const getConfirmText = () => {
    if (currentStatus == 0) {
      return (
        <div className="w-full mb-4">
          <h1 className="text-4xl font-bold">Please confirm the following order</h1>
        </div>
      );
    }
  };

  return (
    <div
      className={clsx(
        'w-full min-h-48 rounded bg-snow-storm-100 shadow-md p-4 transition-all',
        newOrderStyles
      )}>
      <div className="w-full h-full flex flex-col justify-between">
        {getConfirmText()}
        <div className="w-full">
          <div className="w-full flex justify-between">
            <p className="text-2xl font-bold">OrderId</p>
            <p className="text-3xl text-orange-600">{orderid.substring(0, 4)}</p>
          </div>
          <div className="w-full flex justify-between">
            <p className="text-2xl font-bold">Table Number</p>
            <p className="text-3xl text-orange-600">{tableNumber}</p>
          </div>
          <div className="w-full flex justify-between">
            <p className="text-2xl font-bold">{new Date(placedTime * 1000).toISOString()}</p>
          </div>
          <p className="text-2xl font-bold">Items</p>
          <div className="w-full flex flex-col">
            {items.map((item) => (
              <div key={counter++} className="flex justify-between">
                <p className="text-xl">{getItemNameFromId(item['menu-id']).name}</p>
                <p className="text-xl">{item.quantity}</p>
              </div>
            ))}
          </div>
        </div>
        <div className="w-full">
          <Button
            text={currentStatus == 0 ? 'Confirm' : 'Completed'}
            onClick={markComplete}
            icon={CheckCircleIcon}
            className={clsx('w-full', currentStatus == 0 ? 'bg-green-500' : 'bg-orange-500')}
          />
        </div>
      </div>
    </div>
  );
};
export default OrderCard;
