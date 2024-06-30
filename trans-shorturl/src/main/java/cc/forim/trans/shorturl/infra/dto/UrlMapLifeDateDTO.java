package cc.forim.trans.shorturl.infra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * UrlMap生命周期dto
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlMapLifeDateDTO {

    private Long id;

    private Long userId;

    private String requestId;
}
