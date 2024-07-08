import { configureStore } from "@reduxjs/toolkit";
import { combineReducers } from "redux";
import { authReducer, carReducer } from "./slices";
import { sellerReducer } from "./slices/seller.slice";

const rootReducer = combineReducers({
    carReducer: carReducer,
    sellerReducer: sellerReducer,
    authReducer: authReducer,
})

const setupStore = () => configureStore({
    reducer: rootReducer
})

type RootState = ReturnType<typeof rootReducer>
type AppStore = ReturnType<typeof setupStore>
type AppDispatch = AppStore['dispatch']

export type {
    AppDispatch, AppStore, RootState
};

export {
    setupStore
};
