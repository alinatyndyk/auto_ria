import { createAsyncThunk, createSlice, isRejectedWithValue } from "@reduxjs/toolkit";
import { AxiosError } from "axios";
import { IMessage } from "../../components/cars";
import { IError } from "../../interfaces";
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
    regions: IGeoRegion[],
    cities: IGeoCity[],
    totalPages: number,
    user: IUserResponse | null,
}

const initialState: IState = {
    userAuthotization: null,
    errors: null,
    errorGetById: null,
    errorDeleteById: null,
    errorUpdateById: null,
    trigger: false,
    messages: [],
    regions: [],
    cities: [],
    totalPages: 0,
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
            .addCase(getByToken.fulfilled, (state, action) => {
                state.user = action.payload;
                state.userAuthotization = action.payload;
                state.trigger = !state.trigger;
            })
            .addCase(deleteById.fulfilled, () => {
                localStorage.clear();
            })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                if (action.type === "sellerSlice/getById/rejected") {
                    state.errorGetById = action.payload as IError;
                } else if (action.type === "sellerSlice/updateById/rejected") {
                    state.errorUpdateById = action.payload as IError;
                } else if (action.type === "sellerSlice/deletedById/rejected") {
                    state.errorDeleteById = action.payload as IError;
                }  else {
                    state.errors = action.payload as IError;
                }
            })

});


const { actions, reducer: sellerReducer } = slice;

const sellerActions = {
    ...actions,
    getById,
    getByToken,
    getRegionsByPrefix,
    getRegionsPlaces,
    updateById,
    deleteById
}

export {
    sellerActions,
    sellerReducer
};

