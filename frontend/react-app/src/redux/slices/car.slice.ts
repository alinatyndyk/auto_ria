import {ICar, ICarResponse, ICreateCar, IError} from "../../interfaces";
import {createAsyncThunk, createSlice, isRejectedWithValue} from "@reduxjs/toolkit";
import {carService} from "../../services";
import {AxiosError} from "axios";

interface IState {
    cars: ICar[],
    errors: IError | null,
    trigger: boolean,
    pageCurrent: number,
    pagesInTotal: number,
    carForUpdate: ICar | null
}

const initialState: IState = {
    cars: [],
    errors: null,
    carForUpdate: null,
    trigger: false,
    pageCurrent: 0,
    pagesInTotal: 0,
}

const getAll = createAsyncThunk<ICarResponse, number>(
    'carSlice/getAll',
    async (page: number, {rejectWithValue}) => {
        try {
            console.log("24");
            console.log(page);
            const {data} = await carService.getAll(page);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getBySeller = createAsyncThunk<ICarResponse, { id: number, page: number }>(
    'carSlice/getBySeller',
    async ({id, page}, {rejectWithValue}) => {
        try {
            const {data} = await carService.getBySeller(id, page);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const create = createAsyncThunk<ICar, ICreateCar>(
    'carSlice/create',
    async (car, {rejectWithValue}) => {
        try {
            console.log("39");
            console.log(car, "car");
            const {data} = await carService.create(car);
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
            .addCase(getAll.fulfilled, (state, action) => {
                console.log(action.payload)
                state.cars = action.payload.content;
                state.pageCurrent = action.payload.pageable.pageNumber;
                state.pagesInTotal = action.payload.totalPages;
            })
            .addCase(getBySeller.fulfilled, (state, action) => {
                state.cars = action.payload.content;
                state.pageCurrent = action.payload.pageable.pageNumber;
                state.pagesInTotal = action.payload.totalPages;
                state.trigger = !state.trigger;
                console.log(state.pageCurrent, state.pagesInTotal)
            })
            .addCase(create.fulfilled, (state, action) => {
                state.trigger = !state.trigger;
            })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                state.errors = action.payload as IError;
            })
});


const {actions, reducer: carReducer} = slice;

const carActions = {
    ...actions,
    getAll,
    getBySeller,
    create
}

export {
    carActions,
    carReducer
}