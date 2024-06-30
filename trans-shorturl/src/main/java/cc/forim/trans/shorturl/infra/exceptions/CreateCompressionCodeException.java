package cc.forim.trans.shorturl.infra.exceptions;

import lombok.*;

/**
 * 创建压缩码异常
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class CreateCompressionCodeException extends RuntimeException {

    private Integer code;
    private String message;

}
