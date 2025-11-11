/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#667eea',
          dark: '#5568d3',
          light: '#8b9aef',
        },
        secondary: {
          DEFAULT: '#764ba2',
          dark: '#5c3a7e',
          light: '#9469c4',
        },
      },
    },
  },
  plugins: [],
}
