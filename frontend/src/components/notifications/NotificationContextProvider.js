import React, { useState, useContext, createContext, useReducer } from 'react';
import PropTypes from 'prop-types';

const NotificationContext = createContext();

export const NOTIFICATION_ACTIONS = {
  ADD_NOTIFICATION: 'add-notification',
  REMOVE_NOTIFICATION: 'remove-notification',
  ADD_OPACITY: 'add-opacity',
};

const QUEUE_TIMEOUT = 500;

export const useNotifications = () => {
  return useContext(NotificationContext);
};

const NotificationContextProvider = ({ children }) => {
  const [removingQueue, setRemovingQueue] = useState(0);
  const reducer = (notifications, action) => {
    switch (action.type) {
      case NOTIFICATION_ACTIONS.ADD_NOTIFICATION:
        setTimeout(
          () =>
            notificationDispatch({
              type: NOTIFICATION_ACTIONS.ADD_OPACITY,
              payload: action.payload.id,
            }),
          1500 + removingQueue * QUEUE_TIMEOUT
        );
        setTimeout(
          () =>
            notificationDispatch({
              type: NOTIFICATION_ACTIONS.REMOVE_NOTIFICATION,
              payload: action.payload.id,
            }),
          2500 + removingQueue * QUEUE_TIMEOUT
        );
        setRemovingQueue(removingQueue + 1);
        return [...notifications, action.payload];
      case NOTIFICATION_ACTIONS.ADD_OPACITY:
        for (const not of notifications) {
          if (not.id == action.payload) {
            not.opacity = 'opacity-0';
            setRemovingQueue(removingQueue - 1);
            return [...notifications];
          }
        }
      case NOTIFICATION_ACTIONS.REMOVE_NOTIFICATION:
        return notifications.filter((notification) => notification.id != action.payload);
    }
  };

  const [notifications, notificationDispatch] = useReducer(reducer, []);

  return (
    <NotificationContext.Provider value={{ notifications, notificationDispatch }}>
      {children}
    </NotificationContext.Provider>
  );
};

NotificationContextProvider.propTypes = {
  children: PropTypes.node,
};

export default NotificationContextProvider;
