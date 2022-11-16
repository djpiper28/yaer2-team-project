import React, { useState } from 'react';
import Button from '../ui/Button';
import { AnnotationIcon, MapIcon } from '@heroicons/react/solid';
import TextField from '../ui/TextField';
import { postWaiterNotification } from '../api/ApiClient';

const NotifyWaiter = () => {
  const [problem, setProblem] = useState('');
  const [table, setTable] = useState(1);

  const handleProblem = (e) => {
    setProblem(e.target.value);
  };

  const changeTable = (e) => {
    if (e.target.value < 30 && e.target.value > 0) {
      setTable(e.target.value * 1);
    }
  };

  const submitNotification = (e) => {
    e.preventDefault();
    postWaiterNotification(table, problem)
      .then((r) => {
        console.log(r);
      })
      .catch((e) => console.error(e));
  };

  return (
    <div className="w-full flex justify-center p-4">
      <form
        onSubmit={submitNotification}
        className="w-full max-w-4xl  bg-white rounded flex flex-col items-center">
        <h1 className="text-3xl text-gray-700">Call a waiter</h1>
        <div className="w-full px-4 flex">
          <h1 className="text-xl text-gray-700">Problem</h1>
        </div>
        <div className="w-full px-4">
          <textarea
            className="w-full rounded p-4 border-2 border-gray-700"
            value={problem}
            onChange={handleProblem}></textarea>
        </div>
        <div className="w-full justify-end p-4">
          <h2 className="text-xl text-gray-700">Table Number</h2>
          <TextField
            type="number"
            icon={<MapIcon />}
            min="1"
            max="30"
            value={table}
            onChange={changeTable}
          />
        </div>
        <div className="w-full flex justify-center p-4">
          <Button
            type="submit"
            text="Notify"
            icon={AnnotationIcon}
            className="bg-orange-500 w-3/4"
          />
        </div>
      </form>
    </div>
  );
};
export default NotifyWaiter;
