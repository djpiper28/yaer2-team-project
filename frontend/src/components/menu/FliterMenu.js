import React, { useState, useEffect } from 'react';

const FliterMenu = ({ image, title }) => {
  return (
    <div className="cursor-pointer my-5 rounded-l flex flex-col items-center justify-center transition-all duration-500 ease-in-out">
      <div className="relative mt-2 mx-2">
        <div className="h-56 rounded-2xl overflow-hidden">
          <img src={image} className="object-cover w-full h-full" alt="" />
        </div>
      </div>

      <div className="pt-10 pb-6 w-full px-4">
        <h5 className="font-medium text-2xl text-center leading-none tracking-wider text-black">
          {title}
        </h5>
      </div>
    </div>
  );
};

export default FliterMenu;
