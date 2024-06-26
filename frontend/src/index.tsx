import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import {Provider} from 'react-redux';
import {setupStore} from "./redux";
import {BrowserRouter} from "react-router-dom";
import {MyContextProvider} from "./Context";


const root = ReactDOM.createRoot(
    document.getElementById('root') as HTMLElement
);

const store = setupStore();

root.render(
    <Provider store={store}>
        <BrowserRouter>
            <MyContextProvider>
                <App/>
            </MyContextProvider>
        </BrowserRouter>
    </Provider>
);
reportWebVitals();
