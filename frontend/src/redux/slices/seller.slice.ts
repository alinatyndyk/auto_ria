import { createAsyncThunk, createSlice, isRejectedWithValue } from "@reduxjs/toolkit";
import { AxiosError } from "axios";
import { IMessage } from "../../components/cars";
import { IError } from "../../interfaces";
import {
    IChatResponse,
    IChatsPageResponse,
    IGetChatMessagesRequest,
    IMessagePageResponse
} from "../../interfaces/chat/message.interface";
import { IGeoCitiesResponse, IGeoCity, IGeoRegion } from "../../interfaces/geo.interface";
import { IUserResponse, IUserUpdateRequestWithId } from "../../interfaces/user/seller.interface";
import { authService } from "../../services";
import { sellerService } from "../../services/seller.service";

interface IState {
    userAuthotization: IUserResponse | null,
    errors: IError | null,
    errorGetById: IError | null;
    errorDeleteById: IError | null;
    errorUpdateById: IError | null;
    trigger: boolean,
    messages: IMessage[],
    chats: IChatResponse[],
    regions: IGeoRegion[],
    cities: IGeoCity[],
    totalPages: number,
    chatPage: number,
    user: IUserResponse | null,
    totalPagesMessages: number,
    chatPageMessages: number
}

const initialState: IState = {
    userAuthotization: null,
    errors: null,
    errorGetById: null,
    errorDeleteById: null,
    errorUpdateById: null,
    trigger: false,
    messages: [],
    chats: [],
    regions: [],
    cities: [],
    chatPage: 0,
    totalPages: 0,
    chatPageMessages: 0,
    totalPagesMessages: 0,
    user: null
}

const getById = createAsyncThunk<IUserResponse, number>(
    'sellerSlice/getById',
    async (id: number, { rejectWithValue }) => {
        try {
            const { data } = await sellerService.getById(id);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const deleteById = createAsyncThunk<String, number>(
    'sellerSlice/deletedById',
    async (id: number, { rejectWithValue }) => {
        try {
            const { data } = await sellerService.deleteById(id);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const updateById = createAsyncThunk<IUserResponse, IUserUpdateRequestWithId>(
    'sellerSlice/updateById',
    async ({ id, body }, { rejectWithValue }) => {
        try {
            const { data } = await sellerService.updateById(id, body);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getByToken = createAsyncThunk<IUserResponse, void>(
    'sellerSlice/getByToken',
    async (_, { rejectWithValue }) => {
        try {

            const token = authService.getAccessToken();
            if (token === null) {
                throw new Error("no token");
            }
            const { data } = await sellerService.getByToken(token);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getChatMessages = createAsyncThunk<IMessagePageResponse, IGetChatMessagesRequest>(
    'sellerSlice/getChatMessages',
    async (info, { rejectWithValue }) => {
        try {
            const { data } = await sellerService.getChatMessages(info);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getChatsByUserToken = createAsyncThunk<IChatsPageResponse, number>(
    'sellerSlice/getChatsByUserToken',
    async (page: number, { rejectWithValue }) => {
        try {
            const { data } = await sellerService.getChatsByUserToken(page);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getRegionsByPrefix = createAsyncThunk<IGeoRegion[], string>(
    'sellerSlice/getRegionsByPrefix',
    async (prefix: string, { rejectWithValue }) => {
        try {
            const { data } = await sellerService.getRegionsByPrefix(prefix);
            return data.data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getRegionsPlaces = createAsyncThunk<IGeoCitiesResponse, string>(
    'sellerSlice/getRegionsPlaces',
    async (regionId: string, { rejectWithValue }) => {
        try {
            const { data } = await sellerService.getRegionsPlaces(regionId);
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
                state.userAuthotization = action.payload;
                state.trigger = !state.trigger;
            })
            .addCase(deleteById.fulfilled, () => {
                localStorage.clear();
            })
            // .addMatcher(isRejectedWithValue(), (state, action) => {
            //     state.errors = action.payload as IError;
            // })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                if (action.type === "authSlice/getById/rejected") {
                    state.errorGetById = action.payload as IError;
                } else if (action.type === "authSlice/updateById/rejected") {
                    state.errorUpdateById = action.payload as IError;
                } else if (action.type === "authSlice/deleteById/rejected") {
                    state.errorDeleteById = action.payload as IError;
                } else {
                    state.errors = action.payload as IError;
                }
            })

});


const { actions, reducer: sellerReducer } = slice;

const sellerActions = {
    ...actions,
    getById,
    getByToken,
    getChatMessages,
    getChatsByUserToken,
    getRegionsByPrefix,
    getRegionsPlaces,
    updateById,
    deleteById
}

export {
    sellerActions,
    sellerReducer
};

