import jwt_decode from 'jwt-decode';
import { getAccessToken } from './ApiClient';

export const setToken = (key, value) => {
  localStorage.setItem(key, value);
};

export const getToken = (key) => {
  return localStorage.getItem(key);
};

export const getStoredAccessToken = () => {
  return new Promise((resolve, reject) => {
    try {
      const accessToken = jwt_decode(localStorage.getItem('access'));

      if (accessToken.expires - 10 < new Date().getTime() / 1000) {
        getAccessToken(localStorage.getItem('refresh')).then((r) => {
          setToken('access', r.data['access-token']);
          localStorage.setItem('loggedIn', true);
          resolve(r.data['access-token']);
        });
      } else {
        localStorage.setItem('loggedIn', true);
        resolve(localStorage.getItem('access'));
      }
    } catch (error) {
      getAccessToken(localStorage.getItem('refresh'))
        .then((r) => {
          setToken('access', r.data['access-token']);
          localStorage.setItem('loggedIn', true);
          resolve(r.data['access-token']);
        })
        .catch((e) => {
          localStorage.setItem('loggedIn', false);
          reject();
        });
    }
  });
};

export const logout = () => {
  localStorage.removeItem('access');
  localStorage.removeItem('refresh');
  localStorage.removeItem('loggedIn');
};
