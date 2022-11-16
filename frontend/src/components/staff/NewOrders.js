import React from 'react';
import OrderCard from './OrderCard';
import { useOutletContext } from 'react-router';

/**
 * NewOrders is used in the kitchen UI, to display new incoming orders.
 * All state for that is managed in this component.
 */

const NewOrders = () => {
  const [orders] = useOutletContext();

  const getItemNameFromId = (id) => {
    try {
      return orders['menu-items'][id];
    } catch (error) {
      return null;
    }
  };

  return (
    <div className="w-full flex flex-col">
      <div className="w-full py-4">
        <h1 className="text-7xl text-center text-transparent bg-clip-text bg-gradient-to-r from-orange-500 to-red-900">
          New Orders
        </h1>
      </div>
      <div className="w-full grid xl:grid-cols-3 lg:grid-cols-2 grid-cols-1 gap-4 px-4">
        {orders['new-orders'].map((order) => (
          <OrderCard
            key={order['order-id']}
            orderid={order['order-id']}
            givenNew={order.givenNew}
            tableNumber={order['table-no']}
            items={order['order-lines']}
            placedTime={order['placed-time']}
            getItemNameFromId={getItemNameFromId}
            status={order.status}
          />
        ))}
      </div>
    </div>
  );
};
export default NewOrders;
