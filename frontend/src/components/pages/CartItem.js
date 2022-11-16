import React from 'react';
import { PlusIcon, MinusIcon, TrashIcon } from '@heroicons/react/solid';
import { useOrders, ACTIONS } from '../menu/OrderContextProvider';

const CartItem = ({ name, amount, price, id, image, description }) => {
  const { dispatch } = useOrders();

  const addItem = (giveamount) => {
    if (amount == 1 && giveamount == -1) {
      removeItem();
    } else {
      dispatch({
        type: ACTIONS.ADD_ORDER,
        payload: {
          id: id,
          name: name,
          description: description,
          price: price,
          image: image,
          amount: giveamount,
        },
      });
    }
  };
  const removeItem = () => {
    dispatch({
      type: ACTIONS.DELETE_ORDER,
      payload: id,
    });
  };

  return (
    <div className="w-full my-2 p-2 bg-snow-storm-300 shadow-md text-center items-center grid grid-cols-8">
      <div>Photo</div>
      <div className="col-span-4 text-left pl-10 flex flex-col justify-center items-center">
        <div>{name}</div>
        <div className="flex justify-center items-center gap-4">
          <button className="w-6 h-6" onClick={() => addItem(1)}>
            <PlusIcon />
          </button>
          <p className="text-2xl">{amount}</p>
          <button className="w-6 h-6" onClick={() => addItem(-1)}>
            <MinusIcon />
          </button>
        </div>
      </div>
      <div className="col-span-2">£ {price}</div>
      <button className="w-6 h-6" onClick={() => removeItem(name)}>
        <TrashIcon className="text-red-500" />
      </button>
    </div>
  );
};

export default CartItem;
