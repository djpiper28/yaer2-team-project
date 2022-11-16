import React, { useRef } from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';

const TextField = ({
  type,
  label,
  error,
  icon,
  placeHolder,
  value,
  onChange,
  onFocus,
  ...rest
}) => {
  const textfieldRef = useRef(null);

  const usedIcon = icon;
  const errorClasses = error ? 'border-red-500 border-2' : '';

  return (
    <>
      <label className="block mb-2 text-xl font-medium text-gray-900 dark:text-gray-300">
        {label}
      </label>
      <div className="flex">
        <div className="w-10 px-2 flex justify-center items-center rounded bg-snow-storm-300">
          {usedIcon}
        </div>
        <input
          ref={textfieldRef}
          type={type}
          value={value}
          onChange={onChange}
          onFocus={onFocus}
          {...rest}
          className={clsx(
            'rounded-none rounded-r-lg bg-gray-50 border text-gray-900 first-letter:focus:ring-blue-500',
            'focus:border-blue-500 block flex-1 min-w-0 w-full text-sm border-gray-300 p-2.5 dark:bg-gray-700',
            'dark:focus:ring-blue-500 dark:focus:border-blue-500 transition-all duration-500',
            errorClasses
          )}
          placeholder={placeHolder}
        />
      </div>
    </>
  );
};

TextField.propTypes = {
  type: PropTypes.string,
  label: PropTypes.string,
  icon: PropTypes.any,
  placeHolder: PropTypes.string,
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  onChange: PropTypes.func,
};

export default TextField;
