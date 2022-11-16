import React, { useState } from 'react';
import TextField from '../ui/TextField';
import { AtSymbolIcon, LoginIcon, LockClosedIcon } from '@heroicons/react/solid';

import { postLogin, getAccessToken } from '../api/ApiClient';
import Button from '../ui/Button';
import { setToken } from '../api/TokenHandler';
import { Link, useNavigate } from 'react-router-dom';

const Login = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(false);

  const onLogin = async (e) => {
    e.preventDefault();

    try {
      const login = await postLogin(email, password);
      setToken('refresh', login.data['refresh-token']);
      const accessToken = await getAccessToken(login.data['refresh-token']);
      setToken('access', accessToken.data['access-token']);
      localStorage.setItem('loggedIn', true);
      navigate('/order');
    } catch (error) {
      setError(true);
    }
  };

  const removeError = () => {
    setError(false);
  };

  return (
    <form onSubmit={onLogin} className="w-full h-full flex flex-col justify-center items-center">
      <div className="w-full p-4 flex flex-col rounded bg-snow-storm-100 shadow-md">
        <h1 className="text-4xl text-center">Login</h1>
        <TextField
          label="Email"
          type="text"
          error={error}
          onFocus={removeError}
          icon={<AtSymbolIcon />}
          placeHolder="Email Address..."
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <TextField
          label="Password"
          type="password"
          error={error}
          onFocus={removeError}
          icon={<LockClosedIcon />}
          placeHolder="Password..."
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <div className="flex justify-center items-center w-full p-4">
          <Button className="w-full bg-orange-500" text="Login" type="submit" icon={LoginIcon} />
        </div>
        <div className="flex w-full justify-center items-center">
          <Link to="/auth/register">
            <p className="text-sm text-center text-orange-700">Don't have an account? Sign up!</p>
          </Link>
        </div>
      </div>
    </form>
  );
};
export default Login;
