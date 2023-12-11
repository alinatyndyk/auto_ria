const baseURL = 'http://localhost:8080';
const geoURL = 'http://geodb-free-service.wirefreethought.com/v1/geo/countries/UA';

const cars = 'cars';
const brands = 'brands';
const auth = 'auth';
const sellers = 'sellers';
const customers = 'customers';
const common = 'common';
const chats = 'chats';
const regions = 'regions';

const urls = {
    cars: {
        cars,
        all: (page: number): string => `${cars}/page/${page}`,
        allBrands: (): string => `${cars}/${brands}`,
        allModelsByBrand: (brand: string): string => `${cars}/${brands}/${brand}/models`,
        byId: (id: number): string => `${cars}/${id}`,
        bySeller: (id: number, page: number): string => `${cars}/by-seller/${id}/page/${page}`,
    },
    auth: {
        auth,
        login: (): string => `/api/v1/${auth}/authenticate`,
        signOut: (): string => `/api/v1/${auth}/sign-out`,
        refresh: (): string => `${auth}/refresh`,

        registerSeller: (): string => `/api/v1/${auth}/register-seller/person`,
        registerCustomer: (): string => `/api/v1/${auth}/register-customer`,
        registerManager: (): string => `/api/v1/${auth}/register-manager`,
        registerAdmin: (): string => `/api/v1/${auth}/register-admin`,

        activateCustomer: (): string => `/api/v1/${auth}/activate-customer-account`,
        activateSeller: (): string => `/api/v1/${auth}/activate-seller-account`,

        generateManager: (): string => `/api/v1/${auth}/code-manager`,
        generateAdmin: (): string => `/api/v1/${auth}/code-admin`,

        changePassword: (): string => `/api/v1/${auth}/change-password1`,
        forgotPassword: (): string => `/api/v1/${auth}/forgot-password`,
        resetPassword: (): string => `/api/v1/${auth}/reset-password`,
    },

    users: {
        common,
        getById: (id: number): string => `${common}/users/${id}`,
        getByToken: (): string => `${common}/users/by-token`,

    },

    sellers: {
        sellers,
        all: (page: number): string => `${sellers}/page/${page}`,
        getById: (id: number): string => `${sellers}/${id}`,
    },

    customers: {
        customers,
        all: (page: number): string => `${customers}/page/${page}`,
        getById: (id: number): string => `${customers}/${id}`,
    },

    chats: {
        chats,
        getChatMessages: (page: number): string => `${chats}/page/${page}`,
        getChatsByUserToken: (page: number): string => `${chats}/of-user/page/${page}`,
    },

    geo: {
        regions,
        getRegionsByPrefix: (prefix: string): string => `${regions}?limit=10&offset=0&namePrefix=${prefix}`,
        getRegionsPlaces: (regionId: string): string => `${regions}/${regionId}/places?limit=10&offset=0`, //todo all pages

    }
};

export {
    baseURL,
    geoURL,
    common,
    cars,
    auth,
    sellers,
    customers,
    chats,
    regions,
    urls
};