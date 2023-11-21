import {IAuthRequest, IAuthResponse, IError} from "../../interfaces";
import {createAsyncThunk, createSlice, isRejectedWithValue} from "@reduxjs/toolkit";
import {authService} from "../../services";
import {AxiosError} from "axios";

interface IState {
    errors: IError | null,
    trigger: boolean,
    isAuth: boolean
}

const initialState: IState = {
    isAuth: false,
    errors: null,
    trigger: false
}

const login = createAsyncThunk<IAuthResponse, IAuthRequest>(
    'authSlice/login',
    async (info, {rejectWithValue}) => {
        try {
            console.log("login admin");
            const {data} = await authService.login(info);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const slice = createSlice({
    name: 'carSlice',
    initialState,
    reducers: {},
    extraReducers: builder =>
        builder
            .addCase(login.fulfilled, (state, action) => {
                state.isAuth = true;
                authService.setTokens({...action.payload});
                window.location.reload();
            })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                state.errors = action.payload as IError;
            })
});


const {actions, reducer: authReducer} = slice;

const authActions = {
    ...actions,
    login
}

export {
    authActions,
    authReducer
}