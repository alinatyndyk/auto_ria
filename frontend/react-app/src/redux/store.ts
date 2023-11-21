import {combineReducers} from "redux";
import {configureStore} from "@reduxjs/toolkit";
import {authReducer, carReducer} from "./slices";

const rootReducer = combineReducers({
    carReducer: carReducer,
    authReducer: authReducer
})

const setupStore = () => configureStore({
    reducer: rootReducer
})

type RootState = ReturnType<typeof rootReducer>
type AppStore = ReturnType<typeof setupStore>
type AppDispatch = AppStore['dispatch']

export type {
    RootState,
    AppStore,
    AppDispatch
}

export {
    setupStore
}