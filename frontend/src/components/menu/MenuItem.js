import React, { useState, useEffect } from 'react';
import { useNotifications } from '../notifications/NotificationContextProvider';
import PropTypes from 'prop-types';
import { backendUrl } from '../api/ApiClient';
import { ACTIONS, useOrders } from './OrderContextProvider';
import { nanoid } from 'nanoid';
import { NOTIFICATION_ACTIONS } from '../notifications/NotificationContextProvider';
import { SparklesIcon } from '@heroicons/react/solid';
import clsx from 'clsx';

const newCutoff = 60 * 5;

const MenuItem = ({ title, description, price, image, inStock, addedAt, id }) => {
  const { notificationDispatch } = useNotifications();
  const { orders, dispatch } = useOrders();
  const [numInCart, setNumInCart] = useState(0);

  useEffect(() => {
    const item = orders.filter((i) => i.id == id);
    if (item.length > 0) {
      setNumInCart(item[0].amount);
    } else {
      setNumInCart(0);
    }
  }, [orders]);

  const addNotification = () => {
    notificationDispatch({
      type: NOTIFICATION_ACTIONS.ADD_NOTIFICATION,
      payload: {
        id: nanoid(),
        title: `${title} added to cart!`,
        description: `${title} has been added to your basket!`,
        buttonText: 'Okay',
        opacity: 'opacity-100',
      },
    });
  };

  const addMenuItem = () => {
    dispatch({
      type: ACTIONS.ADD_ORDER,
      payload: {
        id: id,
        name: title,
        description: description,
        price: price,
        image: image,
        amount: 1,
      },
    });
  };

  const getNewItemStyle = () => {
    if (addedAt + newCutoff > new Date().getTime() / 1000) {
      return 'border-2 border-green-500';
    } else {
      return '';
    }
  };

  const getNewItemText = () => {
    if (addedAt + newCutoff > new Date().getTime() / 1000) {
      return (
        <div className="w-full px-2 flex gap-4 justify-start">
          <SparklesIcon className="w-8 h-8 text-emerald-500" />
          <h2 className="font-semibold text-2xl text-emerald-500">New Item</h2>
        </div>
      );
    } else {
      return <></>;
    }
  };

  return (
    <div
      className={clsx(
        'w-full p-2 rounded shadow-lg bg-snow-storm-100 cursor-pointer duration-200 transition-all',
        inStock ? 'hover:scale-105' : '',
        numInCart > 0 ? 'border-l-[6px] border-l-green-500' : '',
        getNewItemStyle()
      )}
      onClick={() => {
        if (inStock) {
          addMenuItem();
          addNotification();
        }
      }}>
      {getNewItemText()}
      <div className="w-full flex flex-col justify-center items-center">
        <div className="w-full flex justify-between">
          <div className="md:w-36 md:min-h-28 w-24 min-h-12 flex-shrink-0 flex-grow-0 p-2">
            <img
              className={clsx(
                'object-cover rounded-xl shadow-lg aspect-square h-full',
                inStock ? '' : 'grayscale'
              )}
              src={backendUrl + image}
            />
          </div>
          <div className="w-full flex lg:flex-row md:flex-col py-2">
            <h2 className="text-xl font-semibold select-none">
              <span className="text-2xl text-green-500 pr-2">
                {numInCart > 0 ? numInCart + 'x' : ''}
              </span>
              {title}
            </h2>
          </div>
        </div>
        <div className="w-full h-full px-4 flex flex-col">
          <div className="h-full">
            <p className="text-md select-none">{description}</p>
          </div>
          <div className="h-full flex flex-col-reverse">
            <p className="text-2xl font-bold">£{price}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

MenuItem.propTypes = {
  title: PropTypes.string,
  description: PropTypes.string,
  price: PropTypes.number,
  image: PropTypes.string,
  id: PropTypes.string,
};

export default MenuItem;
