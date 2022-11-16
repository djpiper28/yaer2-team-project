import React, { useEffect, useState } from 'react';
import { getViewOrder } from '../api/ApiClient';
import CustOrderCard from './CustOrderCard';
import OrderItem from './OrderItem';

const ViewOrder = () => {
  const [order, setOrder] = useState([]);

  useEffect(() => {
    const orderInterval = setInterval(() => {
      getViewOrder().then((response) => {
        setOrder(response.data);
        console.log(response.data);
      });
    }, 2000);

    return () => {
      clearInterval(orderInterval);
    };
  }, []);

  return (
    <div className="w-full flex flex-col items-center">
      <div className="w-full flex flex-col items-center max-w-4xl">
        {order.map((items, index) => (
          <div className="w-full px-5 flex flex-col items-center mb-5 text-gray-600 text-lg rounded bg-snow-storm-100 shadow-md p-4 ">
            <>
              <CustOrderCard
                tableNo={items['table-number']}
                Status={items.status}
                index={index}
                lastTime={items['last-changed-time']}
              />
              {order[Number(index)]['order-lines'].map((order, index) => (
                <OrderItem
                  key={order.id}
                  id={order.id}
                  index={index + 1}
                  name={order['menu-item'].name}
                  amount={order.quantity}
                />
              ))}
            </>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ViewOrder;
