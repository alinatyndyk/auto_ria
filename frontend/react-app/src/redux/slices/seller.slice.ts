import {IError} from "../../interfaces";
import {createAsyncThunk, createSlice, isRejectedWithValue} from "@reduxjs/toolkit";
import {AxiosError} from "axios";
import {ISellerResponse} from "../../interfaces/user/seller.interface";
import {sellerService} from "../../services/seller.service";
import {ICustomerResponse} from "../../interfaces/user/customer.interface";
import {IAdminResponse} from "../../interfaces/user/admin.interface";
import {IManagerResponse} from "../../interfaces/user/manager.interface";
import {IMessage} from "../../components/cars";
import {IChatResponse, IChatsPageResponse, IMessagePageResponse} from "../../interfaces/message.interface";

interface IState {
    errors: IError | null,
    trigger: boolean,
    messages: IMessage[],
    chats: IChatResponse[],
    totalPages: number,
    chatPage: number,
    user: ISellerResponse | ICustomerResponse | IAdminResponse | IManagerResponse | null
    customer: ICustomerResponse | null
}

const initialState: IState = {
    errors: null,
    trigger: false,
    messages: [],
    chats: [],
    chatPage: 0,
    totalPages: 0,
    customer: null,
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

const getCustomerById = createAsyncThunk<ICustomerResponse, number>(
    'sellerSlice/getCustomerById',
    async (id: number, {rejectWithValue}) => {
        try {
            const {data} = await sellerService.getCustomerById(id);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getByToken = createAsyncThunk<ISellerResponse | ICustomerResponse, void>(
    'sellerSlice/getByToken',
    async (_, {rejectWithValue}) => {
        try {
            const {data} = await sellerService.getByToken();
            return data.body;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getChatMessages = createAsyncThunk<IMessagePageResponse, number>(
    'sellerSlice/getChatMessages',
    async (page: number, {rejectWithValue}) => {
        try {
            const {data} = await sellerService.getChatMessages(page);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getChatsByUserToken = createAsyncThunk<IChatsPageResponse, number>(
    'sellerSlice/getChatsByUserToken',
    async (page: number, {rejectWithValue}) => {
        try {
            const {data} = await sellerService.getChatsByUserToken(page);
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
            .addCase(getCustomerById.fulfilled, (state, action) => {
                state.customer = action.payload;
            })
            .addCase(getChatMessages.fulfilled, (state, action) => {
                state.messages = action.payload.content;
                state.totalPages = action.payload.totalPages;
                state.chatPage = action.payload.pageable.pageNumber;
            })
            .addCase(getChatsByUserToken.fulfilled, (state, action) => {
                state.chats = action.payload.content;
                state.totalPages = action.payload.totalPages;
                state.chatPage = action.payload.pageable.pageNumber;
            })
            .addCase(getByToken.fulfilled, (state, action) => {
                console.log(action.payload, "load");
                state.user = action.payload;
                console.log(state.user);
                state.trigger = !state.trigger;
            })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                state.errors = action.payload as IError;
            })
});


const {actions, reducer: sellerReducer} = slice;

const sellerActions = {
    ...actions,
    getById,
    getCustomerById,
    getByToken,
    getChatMessages,
    getChatsByUserToken
}

export {
    sellerActions,
    sellerReducer
}