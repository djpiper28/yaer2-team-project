import React, { useRef } from 'react';
import MenuItem from './MenuItem';

const ItemType = ({ id, description, image, title, items }) => {
  return (
    <div id={id} name={id} className="w-full flex flex-col scroll-m-20">
      <div className="w-full flex justify-start max-h-64">
        <div className="w-64">
          <img src={image} className="object-contain h-full aspect-square" alt="" />
        </div>
        <div className="w-full px-4 py-2 flex flex-col">
          <div className="w-full">
            <h2 className="text-2xl">{title}</h2>
          </div>
          <div className="w-full">
            <p className="text-lg">{description}</p>
          </div>
        </div>
      </div>
      <div className="w-full py-2 grid grid-cols-1 lg:grid-cols-2 2xl:grid-cols-3 gap-2">
        {items.map((item) => (
          <MenuItem
            title={item.name}
            description={item.description}
            image={item['image-uri']}
            inStock={item['in-stock']}
            price={item.price}
            addedAt={item['added-at']}
            id={item.uuid}
            key={item.uuid}
          />
        ))}
      </div>
    </div>
  );
};
export default ItemType;
