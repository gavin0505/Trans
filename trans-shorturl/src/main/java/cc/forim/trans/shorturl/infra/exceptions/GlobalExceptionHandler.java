package cc.forim.trans.shorturl.infra.exceptions;

import cc.forim.trans.common.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * IllegalArgumentException处理
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResultVO<String> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.error(e.getMessage());
        return ResultVO.error(e.getMessage());
    }

    @ExceptionHandler(value = InsertException.class)
    public ResultVO<String> insertExceptionHandler(InsertException e) {
        log.error(e.getMessage());
        return ResultVO.error(e.getMessage());
    }

    @ExceptionHandler(value = SelectException.class)
    public ResultVO<String> selectExceptionHandler(InsertException e) {
        log.error(e.getMessage());
        return ResultVO.error(e.getMessage());
    }

    @ExceptionHandler(value = CreateCompressionCodeException.class)
    public ResultVO<String> createCompressionCodeExceptionHandler(CreateCompressionCodeException e) {
        log.error(e.getMessage());
        return ResultVO.error(e.getMessage());
    }

    @ExceptionHandler(value = CreateShortUrlException.class)
    public ResultVO<String> createShortUrlExceptionHandler(CreateShortUrlException e) {
        log.error(e.getMessage());
        return ResultVO.error(e.getMessage());
    }
}
