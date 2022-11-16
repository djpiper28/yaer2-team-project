import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Component to display all the options for the kitchen staff.
 * TODO: Merge this with the current top bar and custom options.
 */

const StaffNavBar = () => {
  return (
    <div className="w-full h-16 flex justify-between items-center py-2 px-4 bg-gray-900">
      <div className="w-full text-snow-storm-300 text-5xl font-bold align-middle">
        Oxana - Staff
      </div>
      <div className="w-full h-full flex justify-end gap-16 items-center mr-10">
        <Link to="/staff/orders">
          <p className="text-xl text-snow-storm-300">New Orders</p>
        </Link>
        <Link to="/staff/notifications">
          <p className="text-xl text-snow-storm-300">Notifications</p>
        </Link>
      </div>
    </div>
  );
};
export default StaffNavBar;
