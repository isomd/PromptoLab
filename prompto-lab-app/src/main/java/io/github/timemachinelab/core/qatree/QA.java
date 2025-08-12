package io.github.timemachinelab.core.qatree;

import lombok.Data;

@Data
public abstract class QA {

    protected final QAType type;

    protected long createTime;

    protected long updateTime;

    public QA(QAType type) {
        this.type = type;
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
    }

    /**
     * 更新updateTime为当前时间戳
     */
    public void updateTimestamp() {
        this.updateTime = System.currentTimeMillis();
    }
}
