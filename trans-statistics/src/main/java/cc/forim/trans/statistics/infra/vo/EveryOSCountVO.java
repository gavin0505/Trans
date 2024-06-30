package cc.forim.trans.statistics.infra.vo;

import cc.forim.trans.statistics.infra.domain.OSRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 每个操作系统的访问量
 *
 * @author Gavin Zhang
 * @version V1.0
 * @since 2024/4/9 上午11:06
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EveryOSCountVO {

    private Long count;

    private List<OSRecord> osRecords;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;
}
