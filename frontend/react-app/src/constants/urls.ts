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
        login: (): string => `${auth}/authenticate/admin`,
        refresh: (): string => `${auth}/refresh/admin`
    }
};

export {
    baseURL,
    cars,
    auth,
    urls
};