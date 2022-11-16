import React from 'react';
import {
  ViewBoardsIcon,
  ShoppingCartIcon,
  LoginIcon,
  UserCircleIcon,
  CollectionIcon,
  AnnotationIcon,
} from '@heroicons/react/solid';

export const SidebarPage = [
  {
    title: 'Register',
    path: '/register',
    icon: <UserCircleIcon className="w-8" />,
    cName:
      'decoration-none text-snow-storm-300 text-lg w-full mr-4 h-full flex first-letter:items-center px-4 rounded-sm ',
  },
  {
    title: 'Login',
    path: 'login',
    icon: <LoginIcon className="w-8" />,
    cName:
      'decoration-none text-snow-storm-300 text-lg w-full mr-4 h-full flex first-letter:items-center px-4 rounded-sm ',
  },
  {
    title: 'Menu',
    path: '/order',
    icon: <ViewBoardsIcon className="w-8" />,
    cName:
      'decoration-none text-snow-storm-300 text-lg w-full h-full flex first-letter:items-center px-4 rounded-sm ',
  },
  {
    title: 'Cart',
    path: '/cart',
    icon: <ShoppingCartIcon className="w-8" />,
    cName:
      'decoration-none text-snow-storm-300 text-lg w-full mr-4 h-full flex first-letter:items-center px-4 rounded-sm ',
  },
  {
    title: 'Orders',
    path: '/view-orders',
    icon: <CollectionIcon className="w-8" />,
    cName:
      'decoration-none text-snow-storm-300 text-lg w-full mr-4 h-full flex first-letter:items-center px-4 rounded-sm ',
  },
  {
    title: 'Call Waiter',
    path: '/notify-waiter',
    icon: <AnnotationIcon className="w-8" />,
    cName:
      'decoration-none text-snow-storm-300 text-lg w-full mr-4 h-full flex first-letter:items-center px-4 rounded-sm ',
  },
];
