import React, { useState, useEffect } from 'react';
import { ACTIONS, useOrders } from '../menu/OrderContextProvider';
import Button from '../ui/Button';
import CartItem from './CartItem';
import { CreditCardIcon } from '@heroicons/react/solid';
import { getAccessToken, postOrder } from '../api/ApiClient';
import TextField from '../ui/TextField';
import { CashIcon, MapIcon } from '@heroicons/react/outline';
import OrderSuccessful from './OrderSuccessful';
import clsx from 'clsx';

const Cart = () => {
  const { orders, dispatch } = useOrders();

  const [total, setTotal] = useState(0);
  const [subtotal, setSubtotal] = useState(0);
  const [serviceFee, setServiceFee] = useState(0);
  const [table, setTable] = useState(1);
  const [processing, setProcessing] = useState(false);
  const [orderSuccess, setOrderSuccess] = useState(false);
  const [orderCopy, setOrderCopy] = useState([]);

  const [loggedIn, setLoggedIn] = useState(false);

  const calcTotal = () => {
    let newTotal = 0;
    for (const item of orders) {
      newTotal += item.amount * item.price;
    }
    setSubtotal(Math.round(newTotal * 100) / 100);
    setTotal(Math.round(newTotal * 100) / 100 + serviceFee);
  };

  useEffect(() => {
    if (localStorage.getItem('loggedIn') == 'true') {
      setLoggedIn(true);
    }
  }, []);

  useEffect(() => {
    calcTotal();
  }, [orders, serviceFee]);

  const submitOrder = async () => {
    const requestItems = [];
    for (const order of orders) {
      requestItems.push({
        'menu-id': order.id,
        quantity: order.amount,
        'special-requests': 'Please work',
      });
    }

    const orderRequest = await postOrder(requestItems, table, '').catch((error) => {
      setProcessing(false);
    });
    if (orderRequest.status == 200) {
      setOrderSuccess(true);
      setOrderCopy(orders);
      dispatch({
        type: ACTIONS.CLEAR_ORDER,
      });
    }
  };

  const changeTable = (e) => {
    if (e.target.value < 30 && e.target.value > 0) {
      setTable(e.target.value * 1);
    }
  };

  // Rendered when the order is successful.
  if (orderSuccess) {
    return <OrderSuccessful order={orderCopy} />;
  }

  return (
    <div className="w-full flex justify-center my-2">
      <div className="w-full max-w-4xl flex flex-col justify-center items-center rounded bg-snow-storm-100 shadow-md p-4 my-4">
        <div className="w-full p-1 px-5 text-gray-600 text-xl  ">
          <div className=" w-full flex sm:flex-row flex-col sm:justify-between border-b-1 border-polar-night-400">
            <div className="text-xl">Table Number:</div>
            <TextField
              type="number"
              icon={<MapIcon />}
              value={table}
              min="0"
              onChange={changeTable}></TextField>
          </div>

          <div className="w-full border-b-2 border-polar-night-400">
            {orders.map((item) => (
              <CartItem
                key={item.id}
                id={item.id}
                image={item.image}
                name={item.name}
                amount={item.amount}
                price={item.price}
                description={item.description}
              />
            ))}
          </div>
          <div className="w-full border-b-2 border-polar-night-400 flex flex-col">
            <div className="w-full flex justify-between">
              <div className="text-l text-gray-600 p-1 text-left px-5"> Sub total : </div>
              <div className="text-l text-gray-600 p-1 justify-self-end px-5">£ {subtotal}</div>
            </div>
            <div className="w-full flex justify-between">
              <div className="text-l text-gray-600 p-1 px-5 w-full">
                <TextField
                  type="number"
                  label="Service Fee"
                  icon={<CashIcon />}
                  value={serviceFee}
                  min="0"
                  onChange={(e) =>
                    setServiceFee(Math.round(Number(e.target.value) * 100) / 100)
                  }></TextField>
              </div>
            </div>
          </div>
        </div>
        <div className="w-full grid grid-cols-2">
          <div className="text-xl text-gray-600 p-1 text-left px-5">Cart Total: </div>
          <div className="text-xl text-gray-600 p-1 justify-self-end px-5">£ {total}</div>
        </div>
        <div className="w-full flex justify-end">
          <Button
            text={clsx({ 'Processing...': processing }, { Pay: !processing })}
            onClick={() => {
              setProcessing(true);
              submitOrder();
            }}
            className={clsx(
              'w-48',
              { 'bg-orange-500': processing && orders.length > 0 },
              { 'bg-emerald-500': !processing && orders.length > 0 && loggedIn },
              { 'bg-emerald-300': orders.length == 0 || !loggedIn }
            )}
            icon={CreditCardIcon}
            disabled={orders.length == 0 || !loggedIn}
          />
        </div>
        <p className={loggedIn ? 'hidden' : 'text-xl text-gray-600'}>
          You must be logged in to order.
        </p>
      </div>
    </div>
  );
};

export default Cart;
