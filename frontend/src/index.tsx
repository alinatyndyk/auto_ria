import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';
import { BrowserRouter } from "react-router-dom";
import App from './App';
import { MyContextProvider } from "./Context";
import './index.css';
import { setupStore } from "./redux";
import reportWebVitals from './reportWebVitals';


const root = ReactDOM.createRoot(
    document.getElementById('root') as HTMLElement
);

const store = setupStore();

root.render(
    <Provider store={store}>
        <BrowserRouter>
            <MyContextProvider>
                <App />
            </MyContextProvider>
        </BrowserRouter>
    </Provider>
);
reportWebVitals();
