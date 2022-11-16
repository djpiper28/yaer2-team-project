import React from 'react';
import NewOrders from '../staff/NewOrders';

/**
 * This component contains structure for the orders page,
 * Used in the Kitchen UI
 */

const StaffOrders = () => {
  return (
    <div className="w-full p-4 flex gap-4">
      <NewOrders />
    </div>
  );
};
export default StaffOrders;
