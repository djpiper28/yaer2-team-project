import React, { useEffect, useContext, useReducer, createContext } from 'react';
import PropTypes from 'prop-types';
import { setToken } from '../api/TokenHandler';

const OrderContext = createContext();

export const useOrders = () => {
  return useContext(OrderContext);
};

export const ACTIONS = {
  ADD_ORDER: 'add-order',
  DELETE_ORDER: 'delete-order',
  CLEAR_ORDER: 'clear-order',
};

const reducer = (orders, action) => {
  switch (action.type) {
    case ACTIONS.ADD_ORDER:
      let exists = false;

      orders.forEach((item) => {
        if (item.id == action.payload.id) {
          item.amount += action.payload.amount;
          exists = true;
        }
      });
      if (exists) {
        return [...orders];
      } else {
        return [...orders, action.payload];
      }
    case ACTIONS.DELETE_ORDER:
      return orders.filter((order) => order.id !== action.payload);
    case ACTIONS.CLEAR_ORDER:
      localStorage.removeItem('order');
      return [];
  }
};

const OrderContextProvider = ({ children }) => {
  const [orders, dispatch] = useReducer(reducer, [], () => {
    const initialState = localStorage.getItem('order');

    if (initialState == undefined || initialState == 'null') {
      setToken('order', JSON.stringify([]));
      return [];
    } else {
      try {
        const basket = JSON.parse(initialState);
        for (const item of basket) {
          if (item.amount <= 0) {
            return [];
          }
        }
        return basket;
      } catch (error) {
        return [];
      }
    }
  });

  useEffect(() => {
    setToken('order', JSON.stringify(orders));
  }, [orders]);

  return <OrderContext.Provider value={{ orders, dispatch }}>{children}</OrderContext.Provider>;
};

OrderContextProvider.propTypes = {
  children: PropTypes.node,
};

export default OrderContextProvider;
