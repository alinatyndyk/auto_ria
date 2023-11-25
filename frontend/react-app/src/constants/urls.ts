const baseURL = 'http://localhost:8080';

const cars = 'cars';
const auth = 'auth';
const sellers = 'sellers';
const common = 'common';

const urls = {
    cars: {
        cars,
        all: (page: number): string => `${cars}/page/${page}`,
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
    }
};

export {
    baseURL,
    common,
    cars,
    auth,
    sellers,
    urls
};