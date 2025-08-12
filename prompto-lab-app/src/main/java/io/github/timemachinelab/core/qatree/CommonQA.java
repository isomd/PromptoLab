package io.github.timemachinelab.core.qatree;

import lombok.Data;

@Data
public class CommonQA extends QA {

    public CommonQA() {
        super(QAType.QA);
    }

    private String question;

    private String answer;


}
