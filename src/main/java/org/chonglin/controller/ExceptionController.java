package org.chonglin.controller;

import org.apache.shiro.ShiroException;
import org.chonglin.exception.UnauthorizedException;
import org.chonglin.util.R;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.List;

@RestControllerAdvice
public class ExceptionController {

    // 捕捉shiro的异常
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ShiroException.class)
    public R handle402(ShiroException e) {
        return R.error("402",e.getMessage());
    }

    // 捕捉UnauthorizedException
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public R handle401() {
        return R.error("401","Unauthorized");
    }

    // 捕捉其他所有异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R globalException(HttpServletRequest request, Throwable ex) {
        ex.printStackTrace();
        //validation 参数验证错误
        if(ex instanceof MethodArgumentNotValidException) {
            BindingResult bindingResult = ((MethodArgumentNotValidException) ex).getBindingResult();
            List<ObjectError> errors = bindingResult.getAllErrors();
            String msg = "参数错误";
            if(!CollectionUtils.isEmpty(errors)) {
                msg = errors.get(0).getDefaultMessage();
            }
            return R.error("601",msg);
        }
        return R.error(getStatus(request).value()+"",ex.getMessage());
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }
}

