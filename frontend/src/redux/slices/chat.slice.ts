import { createAsyncThunk, createSlice, isRejectedWithValue } from "@reduxjs/toolkit";
import { AxiosError } from "axios";
import {
    IError
} from "../../interfaces";
import { IChatResponse, IMsgsOfChatRequest, IMsgsOfChatResponse, MessageClass } from "../../interfaces/chat.interface";
import { chatService } from "../../services/chat.service";

interface IState {
    errors: IError | null,
    chatsByUser: IChatResponse[] | [],
    loadingChats: boolean,
    msgsOfChat: MessageClass[] | [],
    pageCurrent: number,
    pagesInTotal: number,
    pageMsgsCurrent: number,
    pagesMsgsInTotal: number,
}

const initialState: IState = {
    errors: null,
    loadingChats: false,
    chatsByUser: [],
    msgsOfChat: [],
    pageCurrent: 0,
    pagesInTotal: 0,
    pageMsgsCurrent: 0,
    pagesMsgsInTotal: 0,
}

const getChatsByUser = createAsyncThunk(
    'chatlice/getChatsbyUser',
    async (page: number, { rejectWithValue }) => {
        try {
            const { data } = await chatService.getChatsByUser(page);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getMsgsOfChat = createAsyncThunk<IMsgsOfChatResponse, IMsgsOfChatRequest>(
    'chatlice/getMsgsOfChat',
    async ({ page, yourId, secondId }, { rejectWithValue }) => {
        try {
            const { data } = await chatService.getMsgsOfChat(page, yourId, secondId);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const slice = createSlice({
    name: 'chatSlice',
    initialState,
    reducers: {},
    extraReducers: builder =>
        builder
            .addCase(getChatsByUser.fulfilled, (state, action) => {
                state.loadingChats = false;
                state.chatsByUser = action.payload.content;
                state.pageCurrent = action.payload.pageable.pageNumber;
                state.pagesInTotal = action.payload.totalPages;
            })
            .addCase(getChatsByUser.pending, (state) => {
                state.loadingChats = true;
            })
            .addCase(getMsgsOfChat.fulfilled, (state, action) => {
                state.msgsOfChat = action.payload.content;
                state.pageMsgsCurrent = action.payload.pageable.pageNumber;
                state.pagesMsgsInTotal = action.payload.totalPages;

                console.log(JSON.stringify(action.payload.content) + "paulaos");

            })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                if (action.type === "chatlice/getChatsbyUser/rejected") {
                    state.errors = action.payload as IError;
                } else if (action.type === "chatlice/getMsgsOfChat/rejected") {
                    state.errors = action.payload as IError;
                } else {
                    state.errors = action.payload as IError;
                }
            })

});

const { actions, reducer: chatReducer } = slice;

const chatActions = {
    ...actions,
    getChatsByUser,
    getMsgsOfChat,
}

export {
    chatActions,
    chatReducer
};

