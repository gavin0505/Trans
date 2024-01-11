package cc.forim.trans.common.exception;

/**
 * 锁异常类
 *
 * @author Gavin Zhang
 * @version V1.0
 */
public class LockException extends RuntimeException {

    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockException(Throwable cause) {
        super(cause);
    }
}