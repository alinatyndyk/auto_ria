import { createAsyncThunk, createSlice, isRejectedWithValue } from "@reduxjs/toolkit";
import { AxiosError } from "axios";
import { IMessage } from "../../components/cars";
import { IError } from "../../interfaces";
import { EGeoState, IGeoCitiesResponse, IGeoCity, IGeoCityForState, IGeoRegion, IGeoRegionForState, IGeoStateRequest } from "../../interfaces/geo.interface";
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
    carCreateRegions: IGeoRegion[],
    carCreateCities: IGeoCity[],
    carUpdateRegions: IGeoRegion[],
    carUpdateCities: IGeoCity[],
    userCreateRegions: IGeoRegion[],
    userCreateCities: IGeoCity[],
    userUpdateRegions: IGeoRegion[],
    userUpdateCities: IGeoCity[],
    totalPages: number,
    user: IUserResponse | null,
    userConvesation: IUserResponse | null,
    isUserLoading: boolean
}

const initialState: IState = {
    userAuthotization: null,
    errors: null,
    errorGetById: null,
    errorDeleteById: null,
    errorUpdateById: null,
    trigger: false,
    messages: [],
    carCreateRegions: [],
    carCreateCities: [],
    carUpdateRegions: [],
    carUpdateCities: [],
    userCreateRegions: [],
    userCreateCities: [],
    userUpdateRegions: [],
    userUpdateCities: [],
    totalPages: 0,
    user: null,
    userConvesation: null,
    isUserLoading: false
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

const getUserConversation = createAsyncThunk<IUserResponse, number>(
    'sellerSlice/getUserConversation',
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

const getRegionsByPrefix = createAsyncThunk<IGeoRegionForState, IGeoStateRequest>(
    'sellerSlice/getRegionsByPrefix',
    async ({ info, stateToFill }, { rejectWithValue }) => {
        try {
            const { data } = await sellerService.getRegionsByPrefix(info);
            const result: IGeoRegionForState = { regions: data.data, stateToFill }
            return result;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getRegionsPlaces = createAsyncThunk<IGeoCityForState, IGeoStateRequest>(
    'sellerSlice/getRegionsPlaces',
    async ({ info, stateToFill }, { rejectWithValue }) => {
        try {
            const { data } = await sellerService.getRegionsPlaces(info);
            const result: IGeoCityForState = { cities: data.data, stateToFill }
            return result;
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
            .addCase(getById.pending, (state) => {
                state.isUserLoading = true;
            })
            .addCase(getUserConversation.fulfilled, (state, action) => {
                state.userConvesation = action.payload;
            })
            .addCase(getById.fulfilled, (state, action) => {
                state.isUserLoading = false;
                state.user = action.payload;
            })
            .addCase(getRegionsByPrefix.fulfilled, (state, action) => {
                const { regions, stateToFill } = action.payload;
                if (stateToFill === EGeoState.CAR_CREATE) {
                    state.carCreateRegions = regions;
                } else if (stateToFill === EGeoState.CAR_UPDATE) {
                    state.carUpdateRegions = regions;
                } else if (stateToFill === EGeoState.USER_CREATE) {
                    state.userCreateRegions = regions;
                } else if (stateToFill === EGeoState.USER_UPDATE) {
                    state.userUpdateRegions = regions;
                }
            })
            .addCase(getRegionsPlaces.fulfilled, (state, action) => {
                const { cities, stateToFill } = action.payload;
                if (stateToFill === EGeoState.CAR_CREATE) {
                    state.carCreateCities = cities;
                } else if (stateToFill === EGeoState.CAR_UPDATE) {
                    state.carUpdateCities = cities;
                } else if (stateToFill === EGeoState.USER_CREATE) {
                    state.userCreateCities = cities;
                } else if (stateToFill === EGeoState.USER_UPDATE) {
                    state.userUpdateCities = cities;
                }
            })
            .addCase(getByToken.pending, (state) => {
                state.isUserLoading = true;
            })
            .addCase(getByToken.fulfilled, (state, action) => {
                state.isUserLoading = false;
                state.user = action.payload;
                state.userAuthotization = action.payload;
                state.trigger = !state.trigger;
            })
            .addCase(deleteById.fulfilled, () => {
                localStorage.clear();
            })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                if (action.type === "sellerSlice/getById/rejected") {
                    state.isUserLoading = false;
                    state.errorGetById = action.payload as IError;
                } else if (action.type === "sellerSlice/updateById/rejected") {
                    state.errorUpdateById = action.payload as IError;
                } else if (action.type === "sellerSlice/deletedById/rejected") {
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
    getUserConversation,
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

