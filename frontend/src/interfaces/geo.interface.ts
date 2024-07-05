export interface IGeoRegion {
    countryCode: string;
    fipsCode: string;
    isoCode: string;
    name: string;
    wikiDataId: string;
}

export interface IGeoRegionForState {
    regions: IGeoRegion[];
    stateToFill: EGeoState;
}

export interface IGeoCity {
    id: number;
    wikiDataId: string;
    type: string;
    name: string;
    latitude: number;
    longitude: number;
    population: number;
    distance: null | number;
    placeType: string;
}

export interface IGeoCityForState {
    cities: IGeoCity[];
    stateToFill: EGeoState;
}

export interface IGeoStateRequest {
    info: string;
    stateToFill: EGeoState;
}

export enum EGeoState {
    CAR_UPDATE = 'CAR_UPDATE',
    CAR_CREATE = 'CAR_CREATE',
    USER_UPDATE = 'USER_UPDATE',
    USER_CREATE = 'USER_CREATE',
}

export interface IGeoCitiesResponse {
    data: IGeoCity[];
    metadata: { currentOffset: number; totalCount: number; };
}

export interface IGeoRegionsResponse {
    data: IGeoRegion[];
    metadata: { currentOffset: number; totalCount: number; };
}