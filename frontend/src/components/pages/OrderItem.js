import React from 'react';

const OrderItem = ({ name, amount, index }) => {
  return (
    <div className="w-full my-2 p-2 bg-snow-storm-300 shadow-md text-center items-center grid grid-cols-5">
      <div className="w-14 text-left pl-4 border-r-2">{index}</div>
      <div className="col-span-3 text-left">{name}</div>
      <div>{amount}</div>
    </div>
  );
};

export default OrderItem;
