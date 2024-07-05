import { IUserUpdateRequest } from "../interfaces/user/seller.interface";
import { authService } from "../services";

const baseURL = 'http://localhost:8080';
const geoURL = 'http://geodb-free-service.wirefreethought.com/v1/geo/countries/UA';

const cars = 'cars';
const brands = 'brands';
const auth = 'auth';
const sellers = 'users';
const common = 'common';
const regions = 'regions';
const chats = 'chats';

const urls = {
    cars: {
        cars,
        all: (page: number): string => `${cars}/page/${page}`,
        allBrands: (): string => `${cars}/${brands}`,
        allModelsByBrand: (brand: string): string => `${cars}/${brands}/${brand}/models`,
        byId: (id: number): string => `${cars}/${id}`,
        byMiddleId: (id: number): string => `${cars}/middle/${id}`,
        deleteById: (id: number): string => `${cars}/${id}`,
        banById: (id: number): string => `${cars}/ban/${id}`,
        unbanById: (id: number): string => `${cars}/activate/${id}`,
        bySeller: (id: number, page: number): string => `${cars}/by-user/${id}/page/${page}`,
    },
    auth: {
        auth,
        login: (): string => `/api/v1/${auth}/authenticate`,
        signOut: (): string => `/api/v1/${auth}/sign-out`,
        refresh: (): string => `/api/v1/${auth}/refresh`,

        registerSeller: (): string => `/api/v1/${auth}/register-user`,
        registerUserAuth: (): string => `/api/v1/${auth}/register-user/with-authority`,

        activateSeller: (): string => `/api/v1/${auth}/activate-user`,

        generateManager: (): string => `/api/v1/${auth}/code-manager`,
        toManager: (): string => `/api/v1/${auth}/to-auth`,
        generateAdmin: (): string => `/api/v1/${auth}/code-admin`,

        changePassword: (): string => `/api/v1/${auth}/change-passwords`,
        forgotPassword: (): string => `/api/v1/${auth}/forgot-password`,
        resetPassword: (): string => `/api/v1/${auth}/reset-password`,
    },

    users: {
        getById: (id: number): string => `/users/${id}`,
        deleteById: (id: number): string => `/users/${id}`,
        getByToken: (token: string): string => `/users/by-token/${token}`,
        updateById: (id: number): string => `users/${id}`,
    },

    sellers: {
        sellers,
        all: (page: number): string => `${sellers}/page/${page}`,
    },
    chats: {
        chats,
        getChatsByUser: (page: number): string => `${chats}/of-user/page/${page}`,
    },

    geo: {
        regions,
        getRegionsByPrefix: (prefix: string): string => `${regions}?limit=10&offset=0&namePrefix=${prefix}`,
        getRegionsPlaces: (regionId: string): string => `${regions}/${regionId}/places?limit=10&offset=0`,

    }
};

export {
    baseURL,
    geoURL,
    common,
    cars,
    auth,
    sellers,
    regions,
    urls
};