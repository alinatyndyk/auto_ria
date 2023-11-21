import {ICar, ICreateCar, IError} from "../../interfaces";
import {createAsyncThunk, createSlice, isRejectedWithValue} from "@reduxjs/toolkit";
import {carService} from "../../services";
import {AxiosError} from "axios";

interface IState {
    cars: ICar[],
    errors: IError | null,
    trigger: boolean,
    carForUpdate: ICar | null
}

const initialState: IState = {
    cars: [],
    errors: null,
    carForUpdate: null,
    trigger: false
}

const getAll = createAsyncThunk<ICar[], number>(
    'carSlice/getAll',
    async (page: number, {rejectWithValue}) => {
        try {
            console.log("24");
            console.log(page);
            const {data} = await carService.getAll(page);
            return data.content;
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
                state.cars = action.payload;
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
    create
}

export {
    carActions,
    carReducer
}