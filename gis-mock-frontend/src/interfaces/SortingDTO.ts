// frontend/src/interfaces/SortingDTO.ts

/*
  Developer note: Matches the DTO from the backend:
  - field: currently active sort field
  - order: asc/desc
  - available_fields: which fields are valid sorting targets
  - available_orders: which orders are possible
*/

export interface SortingDTO {
    field: string;
    order: string;
    available_fields: Array<{ field: string; label: string }>;
    available_orders: string[];
}
