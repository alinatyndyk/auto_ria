import { createAsyncThunk, createSlice, isRejectedWithValue } from "@reduxjs/toolkit";
import { AxiosError } from "axios";
import {
    IError
} from "../../interfaces";
import { IChatResponse, IChatsResponse } from "../../interfaces/chat.interface";
import { chatService } from "../../services/chat.service";

interface IState {
    errors: IError | null,
    chatsByUser: IChatResponse[] | [],
    pageCurrent: number,
    pagesInTotal: number,
}

const initialState: IState = {
    errors: null,
    chatsByUser: [],
    pageCurrent: 0,
    pagesInTotal: 0,
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

const slice = createSlice({
    name: 'carSlice',
    initialState,
    reducers: {},
    extraReducers: builder =>
        builder
            .addCase(getChatsByUser.fulfilled, (state, action) => {
                state.chatsByUser = action.payload.content;
                state.pageCurrent = action.payload.pageable.pageNumber;
                state.pagesInTotal = action.payload.totalPages;
            })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                if (action.type === "chatlice/getChatsbyUser/rejected") {
                    state.errors = action.payload as IError;
                } else {
                    state.errors = action.payload as IError;
                }
            })

});

const { actions, reducer: chatReducer } = slice;

const chatActions = {
    ...actions,
    getChatsByUser
}

export {
    chatActions,
    chatReducer
};

