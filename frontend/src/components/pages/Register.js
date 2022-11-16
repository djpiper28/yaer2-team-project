import React, { useState } from 'react';
import TextField from '../ui/TextField';
import {
  AtSymbolIcon,
  LoginIcon,
  LockClosedIcon,
  PhoneIcon,
  UserCircleIcon,
} from '@heroicons/react/solid';
import { getAccessToken, postRegister } from '../api/ApiClient';
import Button from '../ui/Button';
import { setToken } from '../api/TokenHandler';
import { Link, useNavigate } from 'react-router-dom';

const Register = () => {
  const navigate = useNavigate();

  const [firstname, setFirstname] = useState('');
  const [surname, setSurname] = useState('');
  const [phonenumber, setPhonenumber] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(false);

  const onRegister = async (e) => {
    e.preventDefault();
    try {
      const register = await postRegister(email, phonenumber, firstname, surname, password);
      setToken('refresh', register.data['refresh-token']);
      const accessToken = await getAccessToken(register.data['refresh-token']);
      setToken('access', accessToken.data['access-token']);
      navigate('/order');
    } catch (error) {
      setError(true);
    }
  };

  return (
    <form
      onSubmit={onRegister}
      className="w-full h-full flex flex-col justify-center items-center ">
      <div className="w-full p-4 flex flex-col rounded bg-snow-storm-100 shadow-md gap-2">
        <h1 className="text-4xl text-center">Register</h1>
        <TextField
          label="Firstname"
          type="text"
          icon={<UserCircleIcon />}
          placeHolder="John"
          value={firstname}
          onChange={(e) => setFirstname(e.target.value)}
        />
        <TextField
          label="Surname"
          type="text"
          icon={<UserCircleIcon />}
          placeHolder="Costa"
          value={surname}
          onChange={(e) => setSurname(e.target.value)}
        />
        <TextField
          label="Phone number"
          type="text"
          icon={<PhoneIcon />}
          placeHolder="Phone number"
          value={phonenumber}
          onChange={(e) => setPhonenumber(e.target.value)}
        />
        <TextField
          label="Email"
          type="text"
          icon={<AtSymbolIcon />}
          placeHolder="Email Address..."
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <TextField
          label="Password"
          type="password"
          icon={<LockClosedIcon />}
          placeHolder="Password..."
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <div className="flex justify-center items-center w-full p-4">
          <Button className="w-full bg-orange-500" text="Register" type="submit" icon={LoginIcon} />
        </div>
        <div className="flex w-full justify-center items-center">
          <Link to="/auth/login">
            <p className="text-sm text-center text-orange-700">Already have an account? Login</p>
          </Link>
        </div>
      </div>
    </form>
  );
};
export default Register;
