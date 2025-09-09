package com.devwonder.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "Success", data);
    }
    
    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, data);
    }
    
    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(false, message, null);
    }
    
    public static <T> BaseResponse<T> error(String message, T data) {
        return new BaseResponse<>(false, message, data);
    }
}