package cc.forim.trans.shorturl.infra.register;

import lombok.extern.slf4j.Slf4j;

/**
 * 雪花算法改造
 * <p>
 * 【6】弃用；【8】随机步长；【31】秒级时间戳；【4】机器ID；【15】序列
 *
 * @author Gavin Zhang
 * @version V1.0
 */

@Slf4j
public class SnowFlake {
    /**
     * 起始的时间戳（秒级）2024-01-01 00:00:00
     */
    private final static long START_STAMP = 1704038400L;

    private final static int MIN = 64;

    private final static int MAX = 255;

    /**
     * 每一部分占用的位数
     */

    //序列号占用的位数
    private final static long SEQUENCE_BIT = 15;
    //机器标识占用的位数
    private final static long MACHINE_BIT = 4;
    // 时间戳占用位数
    private final static long TIMESTAMP_BIT = 31;

    /**
     * 每一部分的最大值
     */
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long TIMESTAMP_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long STEP_LEFT = TIMESTAMP_LEFT + TIMESTAMP_BIT;

    //机器标识
    private final long machineId;
    //序列号
    private long sequence = 0L;
    //上一次时间戳
    private long lastStamp = -1L;

    public SnowFlake(long machineId) {
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     */
    public synchronized long nextId() {
        long currStamp = getNewStamp();
        if (currStamp < lastStamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if (currStamp == lastStamp) {
            //相同秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一秒的序列数已经达到最大
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            //不同秒内，序列号置为0
            sequence = 0L;
        }

        lastStamp = currStamp;

        long step = MIN + (int) (Math.random() * (MAX - MIN));
        return
                step << STEP_LEFT
                        |
                        //时间戳部分
                        (currStamp - START_STAMP) << TIMESTAMP_LEFT
                        //机器标识部分
                        | machineId << MACHINE_LEFT
                        //序列号部分
                        | sequence;
    }

    private long getNextMill() {
        long mill = getNewStamp();
        while (mill <= lastStamp) {
            mill = getNewStamp();
        }
        return mill;
    }

    /**
     * 秒级时间戳
     */
    private long getNewStamp() {
        return System.currentTimeMillis() / 1000;
    }
}