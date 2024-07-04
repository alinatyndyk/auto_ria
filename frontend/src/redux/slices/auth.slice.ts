import { createAsyncThunk, createSlice, isRejectedWithValue } from "@reduxjs/toolkit";
import { AxiosError } from "axios";
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
import { ISellerInput } from "../../interfaces/user/seller.interface";
import { authService } from "../../services";

interface IState {
    errors: IError | null,
    activateSellerErrors: IError | null,
    loginErrors: IError | null,
    signOutErrors: IError | null,
    registerErrors: IError | null,
    generateManagerErrors: IError | null,
    generateAdminErrors: IError | null,
    activateManagerErrors: IError | null,
    activateAdminErrors: IError | null,
    changePasswordErrors: IError | null,
    forgotPasswordErrors: IError | null,
    resetPasswordErrors: IError | null,
    trigger: boolean,
    isAuth: boolean,
    authId: number
}

const initialState: IState = {
    isAuth: false,
    errors: null,
    activateSellerErrors: null,
    loginErrors: null,
    signOutErrors: null,
    registerErrors: null,
    generateManagerErrors: null,
    generateAdminErrors: null,
    activateManagerErrors: null,
    activateAdminErrors: null,
    changePasswordErrors: null,
    forgotPasswordErrors: null,
    resetPasswordErrors: null,
    trigger: false,
    authId: 0
}

const login = createAsyncThunk<IAuthResponse, IAuthRequest>(
    'authSlice/login',
    async (info, { rejectWithValue }) => {
        try {
            const { data } = await authService.login(info);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);


const refresh = createAsyncThunk<IAuthResponse, void>(
    'authSlice/refresh',
    async (_, { rejectWithValue }) => {
        try {
            const refresh = authService.getRefreshToken();
            if (!refresh) {
                return rejectWithValue("refresh_token is required");
            }
            const { data } = await authService.refresh({ refreshToken: refresh });
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const signOut = createAsyncThunk<void, void>(
    'authSlice/logOut',
    async (_, { rejectWithValue }) => {
        try {
            const { data } = await authService.signOut();
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const registerSeller = createAsyncThunk<string, ISellerInput>(
    'authSlice/-',
    async (info, { rejectWithValue }) => {
        try {
            if (info.code != null) {
                const { data } = await authService.registerUserAuth(info);
                return data;
            } else {
                const { data } = await authService.registerSeller(info);
                return data;
            }
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const registerUserAuth = createAsyncThunk<IAuthResponse, ISellerInput>(
    'authSlice/registerUserAuth',
    async (info, { rejectWithValue }) => {
        try {
            if (info.code != null) {
                const { data } = await authService.registerUserAuth(info);
                return data;
            }
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const activateSeller = createAsyncThunk<IAuthResponse, IActivationCode>(
    'authSlice/activateSeller',
    async (code, { rejectWithValue }) => {
        try {
            const { data } = await authService.activateSeller(code);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const generateManager = createAsyncThunk<void, IGenerateCode>(
    'authSlice/generateManager',
    async (email, { rejectWithValue }) => {
        try {
            const { data } = await authService.generateManager(email);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const toManager = createAsyncThunk<void, IGenerateCode>(
    'authSlice/toManager',
    async (email, { rejectWithValue }) => {
        try {
            const { data } = await authService.toManager(email);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const generateAdmin = createAsyncThunk<void, IGenerateCode>(
    'authSlice/generateAdmin',
    async (email, { rejectWithValue }) => {
        try {
            const { data } = await authService.generateAdmin(email);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const changePassword = createAsyncThunk<IAuthResponse, INewPassword>(
    'authSlice/changePassword',
    async (info, { rejectWithValue }) => {
        try {
            const { data } = await authService.changePassword(info);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const forgotPassword = createAsyncThunk<IAuthResponse, IForgotPassword>(
    'authSlice/forgotPassword',
    async (info, { rejectWithValue }) => {
        try {
            const { data } = await authService.forgotPassword(info);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const resetPassword = createAsyncThunk<IAuthResponse, IChangePassword>(
    'authSlice/resetPassword',
    async (info, { rejectWithValue }) => {
        try {
            const { data } = await authService.resetPassword(info);
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
                authService.setTokens({ ...action.payload });
                localStorage.setItem('isAuth', JSON.stringify(true));
            })
            .addCase(refresh.fulfilled, (state, action) => {
                state.isAuth = true;
                authService.setTokens({ ...action.payload });
                localStorage.setItem('isAuth', JSON.stringify(true));
            })
            .addCase(changePassword.fulfilled, (state, action) => {
                state.isAuth = true;
                authService.setTokens({ ...action.payload });
            })
            .addCase(resetPassword.fulfilled, (state, action) => {
                state.isAuth = false;
                authService.setTokens({ ...action.payload });
                localStorage.setItem('isAuth', JSON.stringify(true));
            })
            .addCase(signOut.fulfilled, (state) => {
                state.isAuth = false;
                localStorage.clear();
            })
            .addCase(activateSeller.fulfilled, (state, action) => {
                state.isAuth = true;
                authService.setTokens({ ...action.payload });
                localStorage.setItem('isAuth', JSON.stringify(true));
            })
            .addCase(forgotPassword.fulfilled, (state) => {
                state.isAuth = false;
            })
            .addCase(registerUserAuth.fulfilled, (state, action) => {
                state.isAuth = true;
                authService.setTokens({ ...action.payload });
                localStorage.setItem('isAuth', JSON.stringify(true));
            })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                if (action.type === "authSlice/login/rejected") {
                    state.loginErrors = action.payload as IError;
                } else if (action.type === "authSlice/activateSeller/rejected") {
                    state.activateSellerErrors = action.payload as IError;
                } else if (action.type === "authSlice/logOut/rejected") {
                    state.signOutErrors = action.payload as IError;
                } else if (action.type === "authSlice/registerUserAuth/rejected") {
                    state.registerErrors = action.payload as IError;
                } else if (action.type === "authSlice/registerSeller/rejected") {
                    state.registerErrors = action.payload as IError;
                } else if (action.type === "authSlice/generateAdmin/rejected") {
                    state.generateAdminErrors = action.payload as IError;
                } else if ((action.type === "authSlice/generateManager/rejected")
                    || (action.type === "authSlice/toManager/rejected")) {
                    state.generateManagerErrors = action.payload as IError;
                } else if (action.type === "authSlice/changePassword/rejected") {
                    state.changePasswordErrors = action.payload as IError;
                } else if (action.type === "authSlice/forgotPassword/rejected") {
                    state.forgotPasswordErrors = action.payload as IError;
                } else if (action.type === "authSlice/resetPassword/rejected") {
                    state.isAuth = true;
                    state.resetPasswordErrors = action.payload as IError;
                } else {
                    state.errors = action.payload as IError;
                }
            })

});

const { actions, reducer: authReducer } = slice;

const authActions = {
    ...actions,
    login,
    refresh,
    signOut,
    registerSeller,
    registerUserAuth,
    activateSeller,
    generateManager,
    toManager,
    generateAdmin,
    changePassword,
    forgotPassword,
    resetPassword
}

export {
    authActions,
    authReducer
};

