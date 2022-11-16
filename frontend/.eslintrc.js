module.exports = {
  env: {
    browser: true,
    es2021: true,
  },
  extends: ['plugin:react/recommended', 'google'],
  parserOptions: {
    ecmaFeatures: {
      jsx: true,
    },
    ecmaVersion: 'latest',
    sourceType: 'module',
  },
  plugins: ['react'],
  rules: {
    'max-len': ['error', { ignoreStrings: true }],
    'object-curly-spacing': 'off',
    indent: 'off',
    'comma-dangle': 'off',
  },
  settings: {
    react: {
      version: 'detect',
    },
  },
};
