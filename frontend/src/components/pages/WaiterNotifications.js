import React from 'react';
import { useOutletContext } from 'react-router';
import WaiterNotification from '../staff/WaiterNotification';

const WaiterNotifications = () => {
  const [orders] = useOutletContext();

  console.log(orders);

  return (
    <div className="w-full flex flex-col p-4 gap-4">
      <div className="w-full py-4">
        <h1 className="text-7xl text-center text-transparent bg-clip-text bg-gradient-to-r from-orange-500 to-red-900">
          Waiter Notifications
        </h1>
      </div>
      <div className="w-full grid xl:grid-cols-3 lg:grid-cols-2 grid-cols-1 gap-4 px-4">
        {orders['notifications'].map((notification) => (
          <WaiterNotification
            key={notification.id}
            id={notification.id}
            body={notification.body}
            tableNo={notification['table-no']}
            givenNew={notification.givenNew}
          />
        ))}
      </div>
    </div>
  );
};
export default WaiterNotifications;
