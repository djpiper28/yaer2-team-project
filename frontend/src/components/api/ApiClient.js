import axios from 'axios';
import { getStoredAccessToken } from './TokenHandler';

export const backendUrl = process.env.BACKEND_URL;
const backendApiUrl = process.env.BACKEND_API_URL;

export const getMenu = () => {
  return new Promise((resolve, reject) => {
    axios
      .get(`${backendApiUrl}/menu`)
      .then((response) => {
        resolve(response);
      })
      .catch((error) => {
        reject(error);
      });
  });
};

export const postLogin = async (email, password) => {
  const nonce = await getNonce();
  const loginBody = {
    nonce: nonce.data.nonce,
    email: email,
    password: password,
  };

  return axios.post(`${backendApiUrl}/login`, loginBody);
};

export const getAccessToken = async (refreshToken) => {
  const nonce = await getNonce();
  const accessTokenBody = {
    nonce: nonce.data.nonce,
    'refresh-token': refreshToken,
  };

  return axios.post(`${backendApiUrl}/refresh`, accessTokenBody);
};

export const postRegister = async (email, phonenumber, firstname, surname, password) => {
  const nonce = await getNonce();
  const registerBody = {
    nonce: nonce.data.nonce,
    email: email,
    phonenumber: phonenumber,
    firstname: firstname,
    surname: surname,
    password: password,
  };

  return axios.post(`${backendApiUrl}/register`, registerBody);
};

export const postOrder = async (order, tableNumber, specialRequests = '') => {
  const accessToken = await getStoredAccessToken();
  const nonce = await getNonce();

  const requestBody = {
    nonce: nonce.data.nonce,
    'access-token': accessToken,
    'table-number': tableNumber,
    items: order,
    'special-requests': specialRequests,
  };

  return axios.post(`${backendApiUrl}/order`, requestBody);
};

export const getViewOrder = async () => {
  const accessToken = await getStoredAccessToken();
  const nonce = await getNonce();

  const requestBody = {
    nonce: nonce.data.nonce,
    'access-token': accessToken,
  };

  return axios.post(`${backendApiUrl}/viewcustomerorders`, requestBody);
};

export const postChangeOrderToComplete = async (orderid, status) => {
  const accessToken = await getStoredAccessToken();
  const nonce = await getNonce();

  const requestBody = {
    nonce: nonce.data.nonce,
    'access-token': accessToken,
    'new-status': status,
    'order-id': orderid,
  };

  return axios.post(`${backendApiUrl}/orderstatus`, requestBody);
};

export const postWaiterNotification = async (tableNumber, notification) => {
  const accessToken = await getStoredAccessToken();
  const nonce = await getNonce();

  const requestBody = {
    nonce: nonce.data.nonce,
    'access-token': accessToken,
    'table-no': tableNumber,
    'notif-body': notification,
  };

  return axios.post(`${backendApiUrl}/notify`, requestBody);
};

export const postWaiterRemoveNotification = async (notificationId) => {
  const accessToken = await getStoredAccessToken();
  const nonce = await getNonce();

  const requestBody = {
    nonce: nonce.data.nonce,
    'access-token': accessToken,
    'notif-uuid': notificationId,
  };

  return axios.post(`${backendApiUrl}/rm-notif`, requestBody);
};

export const getNonce = () => {
  return new Promise((resolve, reject) => {
    axios
      .get(`${backendApiUrl}/getnonce`)
      .then((response) => {
        resolve(response);
      })
      .catch((error) => {
        reject(error);
      });
  });
};
