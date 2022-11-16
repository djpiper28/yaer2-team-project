import React from 'react';
import Button from '../ui/Button';
import { NOTIFICATION_ACTIONS, useNotifications } from './NotificationContextProvider';
import { CheckIcon } from '@heroicons/react/solid';
import PropTypes from 'prop-types';
import clsx from 'clsx';

const Notification = ({ notificationId, title, description, opacity, buttonText }) => {
  const { notificationDispatch } = useNotifications();

  const handleClick = () => {
    notificationDispatch({
      type: NOTIFICATION_ACTIONS.REMOVE_NOTIFICATION,
      payload: notificationId,
    });
  };

  return (
    <div
      className={clsx(
        'w-full rounded bg-snow-storm-100 shadow-md p-4 flex flex-col transition-all z-50',
        opacity
      )}>
      <p className="font-bold text-lg">{title}</p>
      <p className="text-sm">{description}</p>
      <Button
        className="bg-indigo-500 hover:bg-indigo-600 w-full h-8 mt-2"
        text={buttonText}
        icon={CheckIcon}
        onClick={() => handleClick()}
      />
    </div>
  );
};

Notification.propTypes = {
  notificationId: PropTypes.string,
  title: PropTypes.string,
  description: PropTypes.string,
  buttonText: PropTypes.string,
};

export default Notification;
