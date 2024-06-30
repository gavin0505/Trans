package cc.forim.trans.statistics.infra.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 日访问记录
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayRecord {

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date date;

    private Long count;
}
