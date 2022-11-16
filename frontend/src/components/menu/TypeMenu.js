import React, { useState, useEffect } from 'react';
import { getMenu } from '../api/ApiClient';
import MenuItem from './MenuItem';

const TypeMenu = () => {
  const [menu, setMenu] = useState([]);

  useEffect(() => {
    getMenu()
      .then((response) => {
        setMenu(response.data.items);
      })
      .catch((error) => console.error(error));
  }, []);

  return (
    <div className="w-full flex flex-col justify-center items-center p-4">
      <div className="flex flex-col p-2 w-full max-w-6xl rounded shadow-lg bg-snow-storm-100">
        <h1 className="text-2xl text-center py-2">Menu</h1>
        <div className="flex flex-col justify-items-center gap-2">
          {menu.map((item) => (
            <MenuItem
              title={item.name}
              description={item.description}
              image={item['image-uri']}
              inStock={item['in-stock']}
              price={item.price}
              id={item.uuid}
              key={item.uuid}
            />
          ))}
        </div>
      </div>
    </div>
  );
};

export default TypeMenu;
