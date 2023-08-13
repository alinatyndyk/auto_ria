import {AsyncThunk, createAsyncThunk, createSlice} from "@reduxjs/toolkit";
import {carsService} from "../../services/cars.service";

const initialState = {
    cars: [],
    car: {},
    errors: null
}

const getAll = createAsyncThunk(
    'carSlice/getAll',
    async (_, {rejectWithValue}) => {
        try {
            const {data} = await carsService.getAll();
            return data.cars
        } catch (e: any) {
            return rejectWithValue(e.response.data);
        }
    }
)

const getById: AsyncThunk<any, any, any> = createAsyncThunk(
    'carSlice/getById',
    async ({_id}) => {
        const {data} = await carsService.getById(_id);
        return data
    }
)

const carSlice = createSlice({
    name: 'carSlice',
    initialState,
    reducers: {},
    extraReducers: (builder) =>
        builder
            .addCase(getAll.fulfilled, (state, action) => {
                state.cars = action.payload;
            })
            .addDefaultCase((state, action) => {
                const [type] = action.type.split('/').splice(-1);
                if (type === 'rejected') {
                    state.errors = action.payload;
                } else {
                    state.errors = null;
                }
            })
});

const {reducer: carReducer, actions: {}} = carSlice;

const carActions = {
    getAll,
    getById
}

export {
    carReducer,
    carActions
}