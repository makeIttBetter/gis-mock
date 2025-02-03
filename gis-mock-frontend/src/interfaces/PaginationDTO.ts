// frontend/src/interfaces/PaginationDTO.ts

/*
  Developer note: Matches the DTO from the backend,
  containing details about the current page, total pages, etc.
*/

export interface PaginationDTO {
    page: number;
    page_size: number;
    total_pages: number;
    total_count: number;
}
