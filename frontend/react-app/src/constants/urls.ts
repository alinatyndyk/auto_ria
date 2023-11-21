const baseURL = 'http://localhost:8080';

const cars = 'cars';

const urls = {
    cars: {
        cars,
        all: (page: number): string => `${cars}/page/${page}`,
        byId: (id: number): string => `${cars}/${id}`
    }
};

export {
    baseURL,
    cars,
    urls
};