import {IError} from "../../interfaces";
import {createAsyncThunk, createSlice, isRejectedWithValue} from "@reduxjs/toolkit";
import {AxiosError} from "axios";
import {ISellerResponse} from "../../interfaces/user/seller.interface";
import {sellerService} from "../../services/seller.service";
import {ICustomerResponse} from "../../interfaces/user/customer.interface";
import {IAdminResponse} from "../../interfaces/user/admin.interface";
import {IManagerResponse} from "../../interfaces/user/manager.interface";
import {IMessage} from "../../components/cars";
import {
    IChatResponse,
    IChatsPageResponse,
    IGetChatMessagesRequest,
    IMessagePageResponse
} from "../../interfaces/chat/message.interface";
import {IGeoCitiesResponse, IGeoCity, IGeoRegion} from "../../interfaces/geo.interface";

interface IState {
    errors: IError | null,
    trigger: boolean,
    messages: IMessage[],
    chats: IChatResponse[],
    regions: IGeoRegion[],
    cities: IGeoCity[],
    totalPages: number,
    chatPage: number,
    user: ISellerResponse | ICustomerResponse | IAdminResponse | IManagerResponse | null
    customer: ICustomerResponse | null,
    seller: ISellerResponse | null,
    totalPagesMessages: number,
    chatPageMessages: number
}

const initialState: IState = {
    errors: null,
    trigger: false,
    messages: [],
    chats: [],
    regions: [],
    cities: [],
    chatPage: 0,
    totalPages: 0,
    chatPageMessages: 0,
    totalPagesMessages: 0,
    customer: null,
    seller: null,
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

const getSellerById = createAsyncThunk<ISellerResponse, number>(
    'sellerSlice/getSellerById',
    async (id: number, {rejectWithValue}) => {
        try {
            const {data} = await sellerService.getSellerById(id);
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

const getChatMessages = createAsyncThunk<IMessagePageResponse, IGetChatMessagesRequest>(
    'sellerSlice/getChatMessages',
    async (info, {rejectWithValue}) => {
        try {
            const {data} = await sellerService.getChatMessages(info);
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

const getRegionsByPrefix = createAsyncThunk<IGeoRegion[], string>(
    'sellerSlice/getRegionsByPrefix',
    async (prefix: string, {rejectWithValue}) => {
        try {
            const {data} = await sellerService.getRegionsByPrefix(prefix);
            return data.data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getRegionsPlaces = createAsyncThunk<IGeoCitiesResponse, string>(
    'sellerSlice/getRegionsPlaces',
    async (regionId: string, {rejectWithValue}) => {
        try {
            const {data} = await sellerService.getRegionsPlaces(regionId);
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
            .addCase(getSellerById.fulfilled, (state, action) => {
                state.seller = action.payload;
            })
            .addCase(getRegionsByPrefix.fulfilled, (state, action) => {
                state.regions = action.payload;
            })
            .addCase(getRegionsPlaces.fulfilled, (state, action) => {
                state.cities = action.payload.data;
            })
            .addCase(getChatMessages.fulfilled, (state, action) => {
                state.messages = action.payload.content;
                state.totalPagesMessages = action.payload.totalPages;
                state.chatPageMessages = action.payload.pageable.pageNumber;
            })
            .addCase(getChatsByUserToken.fulfilled, (state, action) => {
                state.chats = action.payload.content;
                state.totalPages = action.payload.totalPages;
                state.chatPage = action.payload.pageable.pageNumber;
            })
            .addCase(getByToken.fulfilled, (state, action) => {
                state.user = action.payload;
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
    getSellerById,
    getByToken,
    getChatMessages,
    getChatsByUserToken,
    getRegionsByPrefix,
    getRegionsPlaces
}

export {
    sellerActions,
    sellerReducer
}