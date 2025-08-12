package io.github.timemachinelab.core.qatree;

public abstract class QA {

    protected final QAType type;

    protected long createTime;

    protected long updateTime;

    public QA(QAType type) {
        this.type = type;
    }
}
