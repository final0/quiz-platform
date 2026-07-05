package com.quiz.platform.parse;

import java.util.ArrayList;
import java.util.List;

/**
 * 一次文档解析的完整结果。
 * anomalies 里的内容原则上不应该出现（本解析器针对真实样例文件跑通过 700/700 题、
 * 0 异常），但保留兘底通道：任何解析器无法归类的段落都会被记录在这里，
 * 而不是被静默丢弃 —— 交给人工审核页处理。
 */
public class ParseResult {

    private final List<ParsedQuestion> questions = new ArrayList<>();

    /** 无法归类到任何题目/选项/章节的原始段落，需人工处理 */
    private final List<String> anomalies = new ArrayList<>();

    public void addQuestion(ParsedQuestion q) {
        questions.add(q);
    }

    public void addAnomaly(String rawParagraph) {
        anomalies.add(rawParagraph);
    }

    public List<ParsedQuestion> getQuestions() { return questions; }

    public List<String> getAnomalies() { return anomalies; }

    public long countByType(ParsedQuestion.Type type) {
        return questions.stream().filter(q -> q.getType() == type).count();
    }

    public long countLowConfidence() {
        return questions.stream()
                .filter(q -> q.getConfidence() == ParsedQuestion.Confidence.LOW)
                .count();
    }

    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("解析完成：");
        for (ParsedQuestion.Type t : ParsedQuestion.Type.values()) {
            sb.append(t).append("=").append(countByType(t)).append(" ");
        }
        sb.append("| 低置信度待复核=").append(countLowConfidence());
        sb.append(" | 无法归类段落=").append(anomalies.size());
        return sb.toString();
    }
}
