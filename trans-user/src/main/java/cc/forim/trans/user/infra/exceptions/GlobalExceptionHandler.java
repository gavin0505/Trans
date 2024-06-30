package cc.forim.trans.user.infra.exceptions;

import cc.forim.trans.common.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResultVO<Map<String, String>> missingServletRequestParameterExceptionHandler(HttpServletRequest req, MethodArgumentNotValidException ex) {

        //处理后返回错误结果
        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError) -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        // 包装 ResultVO 结果
        return ResultVO.error("500","参数校验错误",
                errorMap);
    }

    /**
     * IllegalArgumentException处理
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResultVO<String> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.info(e.getMessage());
        return ResultVO.error(e.getMessage());
    }
}
