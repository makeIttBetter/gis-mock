export interface PaginatedResponse<T> {
    content: T[];
    page: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
}