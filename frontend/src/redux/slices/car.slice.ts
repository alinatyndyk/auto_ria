import { CarsResponse, ICar, ICarResponse, ICreateCar, IError, IMiddleCarValues, IUpdateCarRequest } from "../../interfaces";
import { createAsyncThunk, createSlice, isRejectedWithValue } from "@reduxjs/toolkit";
import { carService } from "../../services";
import { AxiosError } from "axios";
import { useAppNavigate } from "../../hooks";

interface IState {
    cars: CarsResponse[],
    car: CarsResponse | null,
    brands: string[],
    models: string[],
    carErrors: IError | null,
    trigger: boolean,
    pageCurrent: number,
    pagesInTotal: number,
    carForUpdate: CarsResponse | null,
    errorGetAll: IError | null
    errorGetById: IError | null
    errorDeleteById: IError | null
    errorBanById: IError | null
    errorUnbanById: IError | null
    errorCreate: IError | null
    errorUpdateById: IError | null
    errorGetMiddle: IError | null
    middleValue: IMiddleCarValues | null
}

const initialState: IState = {
    cars: [],
    car: null,
    brands: [],
    models: [],
    carErrors: null,
    carForUpdate: null,
    trigger: false,
    pageCurrent: 0,
    pagesInTotal: 0,
    errorGetById: null,
    errorGetAll: null,
    errorDeleteById: null,
    errorBanById: null,
    errorUnbanById: null,
    errorCreate: null,
    errorUpdateById: null,
    errorGetMiddle: null,
    middleValue: null,
}

const getAll = createAsyncThunk<ICarResponse, number>(
    'carSlice/getAll',
    async (page: number, { rejectWithValue }) => {
        try {
            const { data } = await carService.getAll(page);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getById = createAsyncThunk<CarsResponse, number>(
    'carSlice/getById',
    async (carId: number, { rejectWithValue }) => {
        try {
            const { data } = await carService.getById(carId);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getMiddleById = createAsyncThunk<IMiddleCarValues, number>(
    'carSlice/getMiddleById',
    async (carId: number, { rejectWithValue }) => {
        try {
            const { data } = await carService.getMiddleById(carId);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const deleteById = createAsyncThunk(
    'carSlice/deleteById',
    async (carId: number, { rejectWithValue }) => {
        try {
            const { data } = await carService.deleteById(carId);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const banById = createAsyncThunk(
    'carSlice/banById',
    async (carId: number, { rejectWithValue }) => {
        try {
            const { data } = await carService.banById(carId);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const unbanById = createAsyncThunk(
    'carSlice/unbanById',
    async (carId: number, { rejectWithValue }) => {
        try {
            const { data } = await carService.unbanById(carId);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getAllBrands = createAsyncThunk<string[]>(
    'carSlice/getAllBrands',
    async (_, { rejectWithValue }) => {
        try {
            const { data } = await carService.getAllBrands();
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getAllModelsByBrand = createAsyncThunk<string[], string>(
    'carSlice/getAllModelsByBrand',
    async (brand: string, { rejectWithValue }) => {
        try {
            const { data } = await carService.getAllModelsByBrand(brand);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const getBySeller = createAsyncThunk<ICarResponse, { id: number, page: number }>(
    'carSlice/getBySeller',
    async ({ id, page }, { rejectWithValue }) => {
        try {
            const { data } = await carService.getBySeller(id, page);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const create = createAsyncThunk<ICar, ICreateCar>(
    'carSlice/create',
    async (car, { rejectWithValue }) => {
        try {
            const { data } = await carService.create(car);
            return data;
        } catch (e) {
            const err = e as AxiosError;
            return rejectWithValue(err.response?.data);
        }
    }
);

const update = createAsyncThunk<ICar, IUpdateCarRequest>(
    'carSlice/update',
    async (request, { rejectWithValue }) => {
        try {
            const { data } = await carService.updateById(request.id, request.car);
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
            .addCase(getById.rejected, (state, action) => {
                state.errorGetById = action.payload as IError;
            })
            .addCase(getAllBrands.fulfilled, (state, action) => {
                state.brands = action.payload;
            })
            .addCase(getAllModelsByBrand.fulfilled, (state, action) => {
                state.models = action.payload;
            })
            .addCase(getMiddleById.fulfilled, (state, action) => {
                state.middleValue = action.payload;
            })
            .addCase(getBySeller.fulfilled, (state, action) => {
                state.cars = action.payload.content;
                state.pageCurrent = action.payload.pageable.pageNumber;
                state.pagesInTotal = action.payload.totalPages;
            })
            .addMatcher(isRejectedWithValue(), (state, action) => {
                if (action.type === "carSlice/getAll/rejected") {
                    state.errorGetById = action.payload as IError;
                } else if (action.type === "carSlice/getAll/rejected") {
                    state.errorGetAll = action.payload as IError;
                } else if (action.type === "carSlice/getById/rejected") {
                    state.errorGetById = action.payload as IError;
                } else if (action.type === "carSlice/deleteById/rejected") {
                    state.errorDeleteById = action.payload as IError;
                } else if (action.type === "carSlice/banById/rejected") {
                    state.errorBanById = action.payload as IError;
                } else if (action.type === "carSlice/unbanById/rejected") {
                    state.errorUnbanById = action.payload as IError;
                } else if (action.type === "carSlice/create/rejected") {
                    state.errorCreate = action.payload as IError;
                } else if (action.type === "carSlice/updateById/rejected") {
                    state.errorUpdateById = action.payload as IError;
                } else if (action.type === "carSlice/getMiddleById/rejected") {
                    state.errorGetMiddle = action.payload as IError;
                } else {
                    state.carErrors = action.payload as IError;
                }
            })
});


const { actions, reducer: carReducer } = slice;

const carActions = {
    ...actions,
    getAll,
    getById,
    deleteById,
    banById,
    unbanById,
    getAllBrands,
    getAllModelsByBrand,
    getBySeller,
    create,
    update,
    getMiddleById,
}

export {
    carActions,
    carReducer
}