import React, { useEffect, useState } from 'react';
import { ShoppingCartIcon } from '@heroicons/react/solid';
import { useOrders } from '../menu/OrderContextProvider';
import { useNavigate } from 'react-router';

const BasketIndicator = () => {
  const { orders } = useOrders();
  const navigate = useNavigate();

  const [total, setTotal] = useState(0);

  useEffect(() => {
    let newTotal = 0;
    for (const item of orders) {
      newTotal += item.amount * item.price;
    }
    setTotal(Math.round(newTotal * 100) / 100);
  }, [orders]);

  return (
    <div
      className="flex items-center border-2 p-2 rounded border-green-500 cursor-pointer"
      onClick={() => {
        navigate('/cart');
      }}>
      <ShoppingCartIcon className="w-8 h-8 text-white" />
      <p className="text-lg text-white select-none">£{total}</p>
    </div>
  );
};
export default BasketIndicator;
