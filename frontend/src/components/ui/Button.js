import clsx from 'clsx';
import React from 'react';
import PropTypes from 'prop-types';

const Button = ({ type, text, icon, className, onClick, disabled }) => {
  let Icon = <></>;
  if (icon != undefined) {
    Icon = icon;
  }

  return (
    <button
      className={clsx(
        'flex justify-center items-center gap-2 h-10 px-5 text-indigo-100 transition-colors duration-300 rounded-lg focus:shadow-outline shadow-lg',
        className
      )}
      type={type}
      onClick={onClick}
      disabled={disabled}>
      <p>{text}</p>
      <Icon className="w-4 h-4" />
    </button>
  );
};

Button.propTypes = {
  text: PropTypes.string,
  icon: PropTypes.any,
  className: PropTypes.string,
  onClick: PropTypes.func,
  disabled: PropTypes.bool,
};

export default Button;
