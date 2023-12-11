import {ICar, ICarResponse, ICreateCar, IError} from "../../interfaces";
import {createAsyncThunk, createSlice, isRejectedWithValue} from "@reduxjs/toolkit";
import {carService} from "../../services";
import {AxiosError} from "axios";

interface IState {
    cars: ICar[],
    car: ICar | null,
    brands: string[],
    models: string[],
    errors: IError | null,
    trigger: boolean,
    pageCurrent: number,
    pagesInTotal: number,
    carForUpdate: ICar | null
}

const initialState: IState = {
    cars: [],
    car: null,
    brands: [],
    models: [],
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

const getById = createAsyncThunk<ICar, number>(
    'carSlice/getById',
    async (carId: number, {rejectWithValue}) => {
        try {
            const {data} = await carService.getById(carId);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getAllBrands = createAsyncThunk<string[]>(
    'carSlice/getAllBrands',
    async (_, {rejectWithValue}) => {
        try {
            console.log("hello")
            const {data} = await carService.getAllBrands();
            console.log(data, "slice")
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getAllModelsByBrand = createAsyncThunk<string[], string>(
    'carSlice/getAllModelsByBrand',
    async (brand: string, {rejectWithValue}) => {
        try {
            const {data} = await carService.getAllModelsByBrand(brand);
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
                state.cars = action.payload.content;
                state.pageCurrent = action.payload.pageable.pageNumber;
                state.pagesInTotal = action.payload.totalPages;
            })
            .addCase(getById.fulfilled, (state, action) => {
                state.car = action.payload;
            })
            .addCase(getAllBrands.fulfilled, (state, action) => {
                state.brands = action.payload;
            })
            .addCase(getAllModelsByBrand.fulfilled, (state, action) => {
                state.models = action.payload;
            })
            .addCase(getBySeller.fulfilled, (state, action) => {
                state.cars = action.payload.content;
                state.pageCurrent = action.payload.pageable.pageNumber;
                state.pagesInTotal = action.payload.totalPages;
                state.trigger = !state.trigger;
                console.log(state.pageCurrent, state.pagesInTotal)
            })
            .addCase(create.fulfilled, (state) => {
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
    getById,
    getAllBrands,
    getAllModelsByBrand,
    getBySeller,
    create
}

export {
    carActions,
    carReducer
}