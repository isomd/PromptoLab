package io.github.timemachinelab.core.qatree;

public class SelectionQA extends QA {

    public SelectionQA() {
        super(QAType.SELECTION);
    }

    private String question;

    private String[] options;

    private Integer selectedOption;
}
