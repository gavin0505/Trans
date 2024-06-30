package cc.forim.trans.shorturl.infra.register;

/**
 * 序列生成器
 *
 * @author Gavin Zhang
 * @version V1.0
 */
public interface SequenceGenerator {

    /**
     * 生成整型序列
     *
     * @return long
     */
    long generate();
}