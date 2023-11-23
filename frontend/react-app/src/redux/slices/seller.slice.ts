import {IError} from "../../interfaces";
import {createAsyncThunk, createSlice, isRejectedWithValue} from "@reduxjs/toolkit";
import {AxiosError} from "axios";
import {ISellerResponse} from "../../interfaces/user/seller.interface";
import {sellerService} from "../../services/seller.service";
import {ICustomerResponse} from "../../interfaces/user/customer.interface";
import {IAdminResponse} from "../../interfaces/user/admin.interface";
import {IManagerResponse} from "../../interfaces/user/manager.interface";

interface IState {
    errors: IError | null,
    trigger: boolean,
    user: ISellerResponse | ICustomerResponse | IAdminResponse | IManagerResponse | null
}

const initialState: IState = {
    errors: null,
    trigger: false,
    user: null
}

const getById = createAsyncThunk<ISellerResponse, number>(
    'sellerSlice/getById',
    async (id: number, {rejectWithValue}) => {
        try {
            const {data} = await sellerService.getById(id);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getByToken = createAsyncThunk<any, string>(
    'sellerSlice/getByToken',
    async (token: string, {rejectWithValue}) => {
        try {
            const {data} = await sellerService.getByToken(token);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const slice = createSlice({
    name: 'sellerSlice',
    initialState,
    reducers: {},
    extraReducers: builder =>
        builder
            .addCase(getById.fulfilled, (state, action) => {
                state.user = action.payload;
            })
            .addCase(getByToken.fulfilled, (state, action) => {
                state.user = action.payload;
            })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                state.errors = action.payload as IError;
            })
});


const {actions, reducer: sellerReducer} = slice;

const sellerActions = {
    ...actions,
    getById,
    getByToken
}

export {
    sellerActions,
    sellerReducer
}