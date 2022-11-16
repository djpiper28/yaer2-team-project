import React, { useState, useEffect } from 'react';
import { backendUrl, getMenu } from '../api/ApiClient';
import Cart from '../pages/Cart';
import ItemType from './ItemType';

const Menu = () => {
  const [menu, setMenu] = useState([]);
  const [menuType, setMenuType] = useState([]);

  useEffect(() => {
    getMenu()
      .then((response) => {
        console.log(response);
        setMenu(response.data.items);
        setMenuType(response.data.types);
      })
      .catch((error) => console.error(error));
  }, []);

  const getTypeItems = (typeid) => {
    return menu.filter((item) => item['item-type'] == typeid);
  };

  const handleClick = (id) => {
    const element = document.getElementById(id);
    const parent = document.getElementById('layout-parent');
    parent.scrollTop = element.offsetTop - 120;
  };

  return (
    <div className="w-full flex flex-col items-center">
      <div className="w-full rounded sticky top-2 z-40 shadow-lg bg-snow-storm-100 flex justify-between items-center px-8 py-2 gap-4 md:overflow-x-hidden overflow-x-scroll">
        {menuType.map((items) => (
          <div
            key={items.uuid}
            className="h-full cursor-pointer"
            onClick={() => handleClick(items.uuid)}>
            <h3 className="text-xl px-4 rounded-full text-emerald-500 hover:bg-emerald-500 hover:text-snow-storm-100 transition-all">
              {items.name}
            </h3>
          </div>
        ))}
      </div>
      <div className="w-full grid xl:grid-cols-3 lg:grid-cols-2 grid-cols-1 gap-2 place-items-start py-2">
        <div className="lg:col-span-2 col-span-1 w-full flex flex-col items-center">
          <div className="w-full flex flex-col rounded h-full">
            <div className="flex flex-col p-2 gap-4">
              {menuType.map((items) => (
                <ItemType
                  key={items.uuid}
                  id={items.uuid}
                  title={items.name}
                  description={items.desc}
                  image={backendUrl + items['image-uri']}
                  items={getTypeItems(items.uuid)}
                />
              ))}
            </div>
          </div>
        </div>
        <div className="xl:flex hidden col-span-1 h-auto w-full flex-grow-0 sticky top-10">
          <Cart />
        </div>
      </div>
    </div>
  );
};
export default Menu;
