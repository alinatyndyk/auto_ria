import {
    IActivationCode,
    IAuthRequest,
    IAuthResponse,
    IChangePassword,
    IError,
    IForgotPassword,
    IGenerateCode,
    INewPassword
} from "../../interfaces";
import {createAsyncThunk, createSlice, isRejectedWithValue} from "@reduxjs/toolkit";
import {authService} from "../../services";
import {AxiosError} from "axios";
import {ISellerInput} from "../../interfaces/user/seller.interface";
import {ICustomerInput} from "../../interfaces/user/customer.interface";
import {RegisterManagerPayload} from "../../interfaces/user/manager.interface";
import {RegisterAdminPayload} from "../../interfaces/user/admin.interface";

interface IState {
    errors: IError | null,
    trigger: boolean,
    isAuth: boolean,
    authId: number
}

const initialState: IState = {
    isAuth: false,
    errors: null,
    trigger: false,
    authId: 0
}

const login = createAsyncThunk<IAuthResponse, IAuthRequest>(
    'authSlice/login',
    async (info, {rejectWithValue}) => {
        try {
            console.log("login");
            const {data} = await authService.login(info);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);


const refresh = createAsyncThunk<IAuthResponse, void>(
    'authSlice/refresh',
    async (_, {rejectWithValue}) => {
        try {
            console.log("refresh");
            const refresh = authService.getRefreshToken();
            if (!refresh) {
                return rejectWithValue("refresh_token is required");
            }
            const {data} = await authService.refresh({refreshToken: refresh});
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const signOut = createAsyncThunk<void, void>(
    'authSlice/logOut',
    async (_, {rejectWithValue}) => {
        try {
            const {data} = await authService.signOut();
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const registerSeller = createAsyncThunk<string, ISellerInput>(
    'authSlice/registerSeller',
    async (info, {rejectWithValue}) => {
        try {
            console.log(info, "info");
            const {data} = await authService.registerSeller(info);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const registerManager = createAsyncThunk<IAuthResponse, RegisterManagerPayload>(
    'authSlice/registerManager',
    async ({managerInput, code}: RegisterManagerPayload, {rejectWithValue}) => {
        try {
            const {data} = await authService.registerManager(managerInput, code);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const registerAdmin = createAsyncThunk<IAuthResponse, RegisterAdminPayload>(
    'authSlice/registerAdmin',
    async ({adminInput, code}: RegisterAdminPayload, {rejectWithValue}) => {
        try {
            const {data} = await authService.registerAdmin(adminInput, code);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const registerCustomer = createAsyncThunk<string, ICustomerInput>(
    'authSlice/registerCustomer',
    async (info, {rejectWithValue}) => {
        try {
            console.log(info, "info")
            const {data} = await authService.registerCustomer(info);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const activateCustomer = createAsyncThunk<IAuthResponse, IActivationCode>(
    'authSlice/activateCustomer',
    async (code, {rejectWithValue}) => {
        try {
            console.log(code, "code")
            const {data} = await authService.activateCustomer(code);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const activateSeller = createAsyncThunk<IAuthResponse, IActivationCode>(
    'authSlice/activateSeller',
    async (code, {rejectWithValue}) => {
        try {
            console.log(code, "code")
            const {data} = await authService.activateSeller(code);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const generateManager = createAsyncThunk<void, IGenerateCode>(
    'authSlice/generateManager',
    async (email, {rejectWithValue}) => {
        try {
            console.log(email, "code")
            const {data} = await authService.generateManager(email);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const generateAdmin = createAsyncThunk<void, IGenerateCode>(
    'authSlice/generateAdmin',
    async (email, {rejectWithValue}) => {
        try {
            console.log(email, "code")
            const {data} = await authService.generateAdmin(email);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const changePassword = createAsyncThunk<IAuthResponse, INewPassword>(
    'authSlice/changePassword',
    async (info, {rejectWithValue}) => {
        try {
            const {data} = await authService.changePassword(info);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const forgotPassword = createAsyncThunk<IAuthResponse, IForgotPassword>(
    'authSlice/forgotPassword',
    async (info, {rejectWithValue}) => {
        try {
            console.log(info, "info");
            const {data} = await authService.forgotPassword(info);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const resetPassword = createAsyncThunk<IAuthResponse, IChangePassword>(
    'authSlice/resetPassword',
    async (info, {rejectWithValue}) => {
        try {
            console.log(info, "password")
            const {data} = await authService.resetPassword(info);
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
                localStorage.setItem('isAuth', JSON.stringify(true));
            })
            .addCase(refresh.fulfilled, (state, action) => {
                state.isAuth = true;
                authService.setTokens({...action.payload});
                localStorage.setItem('isAuth', JSON.stringify(true));
            })
            .addCase(changePassword.fulfilled, (state, action) => {
                state.isAuth = true;
                authService.setTokens({...action.payload});
                window.location.reload();
            })
            .addCase(resetPassword.fulfilled, (state, action) => {
                state.isAuth = false;
                authService.setTokens({...action.payload});
                window.location.reload();
            })
            .addCase(signOut.fulfilled, (state) => {
                state.isAuth = false;
                localStorage.clear();
            })
            .addCase(activateCustomer.fulfilled, (state, action) => {
                state.isAuth = true;
                authService.setTokens({...action.payload});
                localStorage.setItem('isAuth', JSON.stringify(true));
            })
            .addCase(activateSeller.fulfilled, (state, action) => {
                state.isAuth = true;
                authService.setTokens({...action.payload});
                localStorage.setItem('isAuth', JSON.stringify(true));
            })
            .addCase(forgotPassword.fulfilled, (state) => {
                state.isAuth = false;
            })
            .addCase(registerManager.fulfilled, (state, action) => {
                state.isAuth = false;
                authService.setTokens({...action.payload});
                localStorage.setItem('isAuth', JSON.stringify(true));
                window.location.reload();
            })
            .addCase(registerAdmin.fulfilled, (state, action) => {
                state.isAuth = false;
                authService.setTokens({...action.payload});
                localStorage.setItem('isAuth', JSON.stringify(true));
                window.location.reload();
            })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                console.log(action.payload, "action payload error auth")
                state.errors = action.payload as IError;
            })
});


const {actions, reducer: authReducer} = slice;

const authActions = {
    ...actions,
    login,
    refresh,
    signOut,
    registerSeller,
    registerCustomer,
    registerManager,
    registerAdmin,
    activateSeller,
    activateCustomer,
    generateManager,
    generateAdmin,
    changePassword,
    forgotPassword,
    resetPassword
}

export {
    authActions,
    authReducer
}