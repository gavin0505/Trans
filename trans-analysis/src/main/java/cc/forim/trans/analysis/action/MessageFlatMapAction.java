package cc.forim.trans.analysis.action;

import cc.forim.trans.analysis.infra.dto.MessageOutputDTO;
import cc.forim.trans.analysis.infra.dto.MessageRecordDTO;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.OS;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.dialect.Props;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

/**
 * 消息映射处理
 *
 * @author Gavin Zhang
 * @version V1.0
 */
@Slf4j
public class MessageFlatMapAction implements FlatMapFunction<String, MessageOutputDTO> {

    /**
     * 内网
     */
    private static final String INNER_NET = "内网";

    /**
     * 未知
     */
    private static final String UNKNOWN = "未知";

    /**
     * Flink刷洗流程
     *
     * @param s         Kafka得到的JSON字符串
     * @param collector 收集器
     */
    @Override
    public void flatMap(String s, Collector<MessageOutputDTO> collector) {

        // Kafka数据反序列化
        MessageRecordDTO msgInfo = JSONUtil.toBean(s, MessageRecordDTO.class);

        log.info("Kafka Consumed Message: {}", msgInfo);
        // 执行转换
        MessageOutputDTO outputDTO = transfer(msgInfo);
        log.info("After Cleaning Message: {}", outputDTO);
        collector.collect(outputDTO);
    }

    /**
     * 数据刷洗
     *
     * @param dto kafka得到的数据
     * @return 刷洗后的数据
     */
    private MessageOutputDTO transfer(MessageRecordDTO dto) {

        MessageOutputDTO output = new MessageOutputDTO();

        // TraceId
        if (ObjectUtil.isNotEmpty(dto.getTraceId())) {
            output.setTraceId(dto.getTraceId());
        } else {
            output.setTraceId(StrUtil.EMPTY);
        }

        // URL处理
        if (ObjectUtil.isNotEmpty(dto.getTargetAddress())) {
            output.setTargetUrl(dto.getTargetAddress());
        } else {
            output.setTargetUrl(StrUtil.EMPTY);
        }

        if (ObjectUtil.isNotEmpty(dto.getIpAddress())) {
            output.setIp(dto.getIpAddress());
        } else {
            output.setIp(StrUtil.EMPTY);
        }

        if (ObjectUtil.isNotEmpty(dto.getShortUrl())) {
            output.setShortUrl(dto.getShortUrl());
        } else {
            output.setShortUrl(StrUtil.EMPTY);
        }

        // UA信息处理
        if (ObjectUtil.isNotEmpty(dto.getUserAgent())) {
            UserAgent agent = UserAgentUtil.parse(dto.getUserAgent());
            if (ObjectUtil.isNotEmpty(dto.getUserAgent())) {
                Browser browser = agent.getBrowser();
                OS os = agent.getOs();

                output.setBrowser(browser.getName());
                output.setBrowserVersion(browser.getVersion(dto.getUserAgent()));
                output.setOs(os.getName());
                output.setOsVersion(os.getVersion(dto.getUserAgent()));
                output.setMobile(agent.isMobile() ? 1 : 0);
            } else {
                output.setBrowser(StrUtil.EMPTY);
                output.setBrowserVersion(StrUtil.EMPTY);
                output.setOs(StrUtil.EMPTY);
                output.setOsVersion(StrUtil.EMPTY);
                output.setMobile(2);
            }
        } else {
            output.setBrowser(StrUtil.EMPTY);
            output.setBrowserVersion(StrUtil.EMPTY);
            output.setOs(StrUtil.EMPTY);
            output.setOsVersion(StrUtil.EMPTY);
            output.setMobile(2);
        }

        output.setUserId(dto.getUserId());
        output.setUrlMapId(dto.getUrlMapId());

        // 时间戳处理
        output.setDatetime(Long.valueOf(dto.getDatetime()));
        // IP信息处理
        ipQuery(dto.getIpAddress(), output);

        return output;
    }

    /**
     * 调用在线的ip归属地服务
     *
     * @param ip     要查询的ip地址
     * @param output 输出上下文
     */
    private void ipQuery(String ip, MessageOutputDTO output) {
        Props props = new Props("secret.properties");

        // 检测是否为内网ip
        if (!NetUtil.isInnerIP(ip)) {
            // 远程调用ip查询服务
            String json = HttpRequest.get("https://ipcity.market.alicloudapi.com/ip/city/query?ip=" + ip)
                    .header("Authorization", "APPCODE" + " " + props.getProperty("ip_appcode"))
                    .execute().body();
        /*
        以下为服务返回的样例：

       {
            "msg": "成功",
            "success": true,
            "code": 200,
            "data": {
                "orderNo": "169703776398135646",//订单号
                "result": {
                    "continent": "亚洲",//大洲
                    "owner": "中国联通",//所属机构
                    "country": "中国",//国家
                    "lng": "120.208335",//经度
                    "adcode": "330100",//行政编码
                    "city": "杭州市",//城市
                    "timezone": "UTC+8",//时区
                    "isp": "中国联通",//运营商
                    "accuracy": "城市",//精度
                    "source": "数据挖掘",//采集方式
                    "asnumber": "4837",//自治域编码
                    "areacode": "CN",//国家编码
                    "zipcode": "310002",//邮编
                    "radius": "129.2092",//定位半径
                    "prov": "浙江省",//省份
                    "lat": "30.255611"//纬度
                }
            }
        }
         */

            // 解析响应JSON
            JSONObject jsonObject = JSONUtil.parseObj(json);

            if (jsonObject.getInt("code") == 200) {
                // 远程服务没问题，则转换
                output.setIsp(jsonObject.getByPath("result.isp").toString());
                output.setConvince(jsonObject.getByPath("result.prov").toString());
                output.setCity(jsonObject.getByPath("result.city").toString());
            } else {
                // 远程查不到，则未知
                output.setIsp(UNKNOWN);
                output.setConvince(UNKNOWN);
                output.setCity(UNKNOWN);
            }
        } else {
            // 探测到10，127，192的IP头，即内网
            output.setIsp(INNER_NET);
            output.setConvince(INNER_NET);
            output.setCity(INNER_NET);
        }
    }
}
