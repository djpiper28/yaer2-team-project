import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import DefaultLayout from './layout/DefaultLayout';
import CustomerOrder from './pages/CustomerOrder';
import Dashboard from './pages/Dashboard';
import NotificationContextProvider from './notifications/NotificationContextProvider';
import AuthLayout from './layout/AuthLayout';
import Login from './pages/Login';
import Register from './pages/Register';
import Cart from './pages/Cart';
import TypeMenu from './menu/TypeMenu';
import ViewOrder from './pages/ViewOrder';
import KitchenLayout from './layout/KitchenLayout';
import StaffOrders from './pages/StaffOrders';
import ProtectedRoute from './ProtectedRoute';
import NotifyWaiter from './pages/NotifyWaiter';
import WaiterNotifications from './pages/WaiterNotifications';

const App = () => {
  return (
    <NotificationContextProvider>
      <BrowserRouter>
        <Routes>
          <Route path="auth" element={<AuthLayout />}>
            <Route path="login" element={<Login />} />
            <Route path="register" element={<Register />} />
          </Route>
          <Route path="/" element={<DefaultLayout />}>
            <Route path="menu" element={<Dashboard />} />
            <Route path="menu/item" element={<TypeMenu />} />
            <Route path="order" element={<CustomerOrder />} />
            <Route path="notify-waiter" element={<NotifyWaiter />} />
            <Route path="cart" element={<ProtectedRoute />}>
              <Route path="/cart" element={<Cart />} />
            </Route>
            <Route exact path="/" element={<Navigate to="/order" />} />
            <Route path="view-orders" element={<ViewOrder />} />
          </Route>
          <Route path="staff" element={<KitchenLayout />}>
            <Route path="orders" element={<StaffOrders />} />
            <Route path="notifications" element={<WaiterNotifications />} />
            <Route exact path="/staff" element={<Navigate to="/staff/orders" />} />
          </Route>
          <Route path="*" element={<Navigate to="/auth/login" />} />
        </Routes>
      </BrowserRouter>
    </NotificationContextProvider>
  );
};
export default App;
