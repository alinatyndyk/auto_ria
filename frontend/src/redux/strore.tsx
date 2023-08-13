import {combineReducers, configureStore} from '@reduxjs/toolkit';
import { useDispatch } from 'react-redux';

import {carReducer} from "./slices/car.slice";

const rootReducer = combineReducers({
    cars: carReducer
})


const store = configureStore({
    reducer: {
        reducer: rootReducer
    },
});

export type AppDispatch = typeof store.dispatch;
export const useAppDispatch = () => useDispatch<AppDispatch>();

export default store;