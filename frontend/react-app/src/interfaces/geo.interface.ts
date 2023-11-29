export interface IGeoRegion {
    countryCode: string;
    fipsCode: string;
    isoCode: string;
    name: string;
    wikiDataId: string;
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

export interface IGeoCitiesResponse {
    data: IGeoCity[];
    metadata: { currentOffset: number; totalCount: number; };
}

export interface IGeoRegionsResponse {
    data: IGeoRegion[];
    metadata: { currentOffset: number; totalCount: number; };
}