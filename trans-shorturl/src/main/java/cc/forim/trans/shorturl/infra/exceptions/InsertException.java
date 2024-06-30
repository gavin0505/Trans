package cc.forim.trans.shorturl.infra.exceptions;

import lombok.*;

/**
 * 运行时插入存储介质异常
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class InsertException extends RuntimeException {
    private Integer code;
    private String message;
}