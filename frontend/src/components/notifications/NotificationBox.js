import React from 'react';
import Notification from './Notification';
import { useNotifications } from './NotificationContextProvider';

const NotificationBox = () => {
  const { notifications } = useNotifications();

  return (
    <div className="absolute right-4 w-72 flex flex-col-reverse gap-2 p-4">
      {notifications.map((notification) => (
        <Notification
          key={notification.id}
          notificationId={notification.id}
          title={notification.title}
          description={notification.description}
          opacity={notification.opacity}
          buttonText={notification.buttonText}></Notification>
      ))}
    </div>
  );
};
export default NotificationBox;
