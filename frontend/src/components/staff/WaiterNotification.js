import React, { useEffect, useState } from 'react';
import Button from '../ui/Button';
import { CheckCircleIcon } from '@heroicons/react/outline';
import { postWaiterRemoveNotification } from '../api/ApiClient';
import clsx from 'clsx';

const WaiterNotification = ({ id, body, tableNo, givenNew }) => {
  const [newOrderStyles, setNewOrderStyles] = useState(
    givenNew ? 'border-2 border-orange-500' : ''
  );

  useEffect(() => {
    if (givenNew) {
      setTimeout(() => {
        setNewOrderStyles('');
      }, 2000);
    }
  }, []);

  const removeNotification = () => {
    postWaiterRemoveNotification(id).then((r) => {
      console.log(r);
    });
  };

  return (
    <div
      className={clsx(
        'w-full min-h-48 rounded bg-snow-storm-100 shadow-md p-4 transition-all',
        'flex flex-col justify-between gap-4',
        newOrderStyles
      )}>
      <div className="w-full flex flex-col gap-2">
        <div className="w-full flex justify-between items-center px-2">
          <h1 className="text-4xl">Table Number</h1>
          <h2 className="text-3xl font-bold">{tableNo}</h2>
        </div>
        <div className="w-full px-2">
          <p className="text-2xl">{body}</p>
        </div>
      </div>
      <div className="w-full">
        <Button
          text="Complete"
          icon={CheckCircleIcon}
          className="w-full bg-orange-500"
          onClick={removeNotification}
        />
      </div>
    </div>
  );
};
export default WaiterNotification;
