// src/interfaces/ProfileRequestDto.ts

export interface ProfileRequestDto {
    username?: string;
    oldPassword?: string;
    newPassword?: string;
    confirmNewPassword?: string;
}
