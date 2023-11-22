const baseURL = 'http://localhost:8080';

const cars = 'cars';
const auth = 'auth';

const urls = {
    cars: {
        cars,
        all: (page: number): string => `${cars}/page/${page}`,
        byId: (id: number): string => `${cars}/${id}`,
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
    }
};

export {
    baseURL,
    cars,
    auth,
    urls
};