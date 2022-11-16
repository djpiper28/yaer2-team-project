import React from 'react';
import { backendUrl } from '../api/ApiClient';

const OrderSuccessful = ({ order }) => {
  return (
    <div className="w-full flex justify-center my-8">
      <div className="w-full max-w-4xl flex flex-col items-center rounded bg-snow-storm-100 shadow-md my-4 p-4">
        <h1 className="text-7xl font-semibold text-center text-transparent bg-clip-text bg-gradient-to-r from-orange-500 to-red-900">
          Thank you!
        </h1>
        <h3 className="text-4xl text-center text-transparent bg-clip-text bg-gradient-to-r from-orange-500 to-red-900">
          Your order has been placed with the kitchen, strap on and wait for your delicious order.
          To recap you ordered these items:
        </h3>
        <div className="w-full flex flex-col">
          {order.map((item) => (
            <div key={item.id} className="w-full flex items-center">
              <div className="w-36 min-h-28 flex-shrink-0 flex-grow-0 p-2">
                <img
                  src={backendUrl + item.image}
                  className="h-full object-cover rounded-xl shadow-lg aspect-square"
                />
              </div>
              <div className="w-full flex flex-col px-4">
                <h2 className="text-xl font-semibold select-none">{item.name}</h2>
                <p className="text-md select-none">Amount: {item.amount}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
export default OrderSuccessful;
