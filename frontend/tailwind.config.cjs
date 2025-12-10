/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                neo: {
                    bg: '#f0f0f0',
                    main: '#FFDEE9',
                    'pink': '#ff90e8',
                    'yellow': '#fff945',
                    'green': '#28a745',
                    'lime': '#b9ff66',
                    'blue': '#22d3ee',
                    'orange': '#ffab70',
                    'dark': '#1a1a1a',
                }
            },
            boxShadow: {
                'neo': '5px 5px 0px 0px rgba(0,0,0,1)',
                'neo-sm': '3px 3px 0px 0px rgba(0,0,0,1)',
                'neo-lg': '8px 8px 0px 0px rgba(0,0,0,1)',
            },
            fontFamily: {
                'display': ['"Space Grotesk"', 'sans-serif'],
            }
        },
    },
    plugins: [],
}
