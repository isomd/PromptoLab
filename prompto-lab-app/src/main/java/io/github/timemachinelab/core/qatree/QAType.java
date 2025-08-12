package io.github.timemachinelab.core.qatree;

public enum QAType {

    QA("QA"),
    SELECTION("Selection"),
    FORM("Form");

    private String name;

    QAType(String name) {
        this.name = name;
    }
}
