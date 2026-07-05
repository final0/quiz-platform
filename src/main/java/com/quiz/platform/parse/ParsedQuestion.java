package com.quiz.platform.parse;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 规则解析引擎产出的单道题目（尚未落库，供审核页展示/编辑用）。
 */
public class ParsedQuestion {

    public enum Type {
        SINGLE,       // 单选题
        MULTIPLE,     // 多选题
        JUDGE,        // 判断题
        SHORT_ANSWER  // 简答题/问答题
    }

    /** 题型 */
    private Type type;

    /** 原文档中的题号（仅用于人工核对，不作为业务主键） */
    private String sourceNum;

    /** 清洗后的题干（已剔除内嵌答案标记） */
    private String stem;

    /** 选项，key为 A/B/C/D，value为选项文本；判断题/简答题为空 */
    private final Map<String, String> options = new LinkedHashMap<>();

    /** 答案：单选为"C"；多选为"ABD"；判断题为"对"/"错"；简答题为null */
    private String answer;

    /**
     * 解析置信度。
     * HIGH   = 结构完整（题干+选项+答案齐全），可直接入库待审核
     * LOW    = 部分字段缺失（如未识别到答案），需要人工修正
     */
    public enum Confidence { HIGH, LOW }
    private Confidence confidence = Confidence.HIGH;

    /** 供人工复核时对照的原始拼接文本（未清洗） */
    private String rawText;

    public ParsedQuestion(Type type, String sourceNum, String rawStem) {
        this.type = type;
        this.sourceNum = sourceNum;
        this.stem = rawStem;
        this.rawText = rawStem;
    }

    public void appendToStem(String text) {
        this.stem += text;
        this.rawText += text;
    }

    public void putOption(String letter, String text) {
        options.put(letter, text);
    }

    public void appendToOption(String letter, String text) {
        options.merge(letter, text, (old, add) -> old + add);
        this.rawText += text;
    }

    public String getLastOptionLetter() {
        String last = null;
        for (String k : options.keySet()) {
            last = k;
        }
        return last;
    }

    // ---- getters / setters ----

    public Type getType() { return type; }

    public String getSourceNum() { return sourceNum; }

    public String getStem() { return stem; }
    public void setStem(String stem) { this.stem = stem; }

    public Map<String, String> getOptions() { return options; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public Confidence getConfidence() { return confidence; }
    public void setConfidence(Confidence confidence) { this.confidence = confidence; }

    public String getRawText() { return rawText; }

    @Override
    public String toString() {
        return "ParsedQuestion{" +
                "type=" + type +
                ", sourceNum='" + sourceNum + '\'' +
                ", stem='" + stem + '\'' +
                ", options=" + options +
                ", answer='" + answer + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
