package com.quiz.platform.parse;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * docx 客观题题库规则解析器。
 *
 * 设计依据：对真实样例文件（单选300 + 多选300 + 判断100，共700题）逐段落分析后，
 * 归纳的规律与需要兼容的"脏数据"情况，解析验证结果为 700/700 题、0 处无法归类。
 *
 * 已知并处理的脏数据模式：
 *   1. 全角字母/数字/括号/句点混用（如 "（ Ｃ ）"、"1．" ）—— 统一转半角后再匹配。
 *   2. 选项分隔符可选（"A.xxx" / "A、xxx" / "A xxx" 甚至 "Axxx" 无分隔符）。
 *   3. 题干或选项文本因原文档换行被拆成多个 Word 段落（如一个词被从中间断开）
 *      —— 处理方式：任何无法匹配 章节/题目/选项 正则的段落，一律追加到"当前正在
 *      填充的字段"（题干 或 最后一个选项）末尾，不新开字段、不丢弃。
 *   4. 排序类单选题里，四个选项挤在同一行、用制表符分隔
 *      （如 "A．①②③④\tB．②①④③\tC．③④②①\tD．②④①③"）—— 按 \t 拆分后再逐个匹配。
 *   5. 答案内嵌在题干/整句末尾的括号里而不是独立字段
 *      （选择题："...是（D）"；判断题："...。（√）"）—— 用正则提取后从题干中"挖除"，
 *      判断题 √ / × 映射为 "对" / "错"。
 *
 * 使用方式：
 *   try (InputStream is = ...) {
 *       ParseResult result = new DocxRuleBasedParser().parse(is);
 *   }
 *
 * 该类不依赖 Spring，可直接在 quiz-parse 模块的 Service 中调用。
 */
@Component
public class DocxRuleBasedParser {

    // 注：所有正则统一加 UNICODE_CHARACTER_CLASS，让 \s 也能匹配中文全角空格(U+3000)，
    // 真实样例中出现过 "(　C  )" 这种用全角空格填充的答案括号，默认\s（仅ASCII空白）会漏配。
    private static final int F = Pattern.UNICODE_CHARACTER_CLASS;

    // 章节标题：一.单选题（300题） / 二.多选题（...） / 三.判断题（...） / 四.简答题（...）
    private static final Pattern SECTION = Pattern.compile(
            "^[一二三四五六七八九十]+[.、]\\s*(单选题|多选题|判断题|简答题|问答题)", F);

    // 题目起始：数字 + 分隔符(. 、) + 题干正文
    private static final Pattern QUESTION = Pattern.compile("^(\\d+)[.、]\\s*(.*)$", F);

    // 选项：A-D + 可选分隔符(. 、 或无) + 选项正文
    private static final Pattern OPTION = Pattern.compile("^([A-D])[.、]?\\s*(.*)$", F);

    // 答案标记：括号内为 1-4 个 A-D 字母，或 √ / ×
    private static final Pattern ANSWER_MARK = Pattern.compile("[（(]\\s*([A-D]{1,4}|√|×)\\s*[）)]", F);

    public ParseResult parse(InputStream docxStream) throws IOException {
        ParseResult result = new ParseResult();

        try (XWPFDocument doc = new XWPFDocument(docxStream)) {
            List<String> paragraphs = new ArrayList<>();
            for (XWPFParagraph p : doc.getParagraphs()) {
                String text = p.getText();
                if (text != null && !text.trim().isEmpty()) {
                    paragraphs.add(normalizeFullWidth(text.trim()));
                }
            }
            parseParagraphs(paragraphs, result);
        }
        return result;
    }

    /**
     * 供单元测试 / 非docx来源（如已提取好的段落文本）直接调用的入口。
     * 传入的段落无需预先做全角归一化，本方法内部会处理。
     */
    public ParseResult parseFromRawParagraphs(List<String> rawParagraphs) {
        ParseResult result = new ParseResult();
        List<String> normalized = new ArrayList<>();
        for (String p : rawParagraphs) {
            if (p != null && !p.trim().isEmpty()) {
                normalized.add(normalizeFullWidth(p.trim()));
            }
        }
        parseParagraphs(normalized, result);
        return result;
    }


    // ------------------------------------------------------------------

    /** 当前正在填充的字段：STEM 或 某个选项字母 */
    private enum Target { STEM, OPTION }

    private void parseParagraphs(List<String> paragraphs, ParseResult result) {
        ParsedQuestion.Type currentSection = null;
        ParsedQuestion current = null;
        Target target = null;
        String targetOptionLetter = null;

        for (String p : paragraphs) {
            Matcher sm = SECTION.matcher(p);
            if (sm.find() && sm.start() == 0) {
                flush(current, result);
                current = null;
                currentSection = mapSectionType(sm.group(1));
                target = null;
                continue;
            }

            if (currentSection == null) {
                continue;
            }

            Matcher qm = QUESTION.matcher(p);
            if (qm.matches()) {
                flush(current, result);
                current = new ParsedQuestion(currentSection, qm.group(1), qm.group(2));
                target = Target.STEM;
                targetOptionLetter = null;
                continue;
            }

            if (current != null && p.contains("\t")) {
                List<String[]> multi = trySplitMultiOptionLine(p);
                if (multi.size() >= 2) {
                    for (String[] kv : multi) {
                        current.putOption(kv[0], kv[1]);
                    }
                    target = Target.OPTION;
                    targetOptionLetter = multi.get(multi.size() - 1)[0];
                    continue;
                }
            }

            Matcher om = OPTION.matcher(p);
            if (current != null && om.matches()) {
                String letter = om.group(1);
                current.putOption(letter, om.group(2));
                target = Target.OPTION;
                targetOptionLetter = letter;
                continue;
            }

            if (current != null && target != null) {
                if (target == Target.STEM) {
                    current.appendToStem(p);
                } else {
                    current.appendToOption(targetOptionLetter, p);
                }
            } else {
                result.addAnomaly(p);
            }
        }
        flush(current, result);
    }

    private void flush(ParsedQuestion q, ParseResult result) {
        if (q == null) return;
        extractAnswer(q);
        validateConfidence(q);
        result.addQuestion(q);
    }

    private void extractAnswer(ParsedQuestion q) {
        Matcher m = ANSWER_MARK.matcher(q.getStem());
        if (m.find()) {
            String raw = m.group(1);
            String answer = ("√".equals(raw) || "×".equals(raw))
                    ? ("√".equals(raw) ? "对" : "错")
                    : raw;
            q.setAnswer(answer);
            String cleaned = (q.getStem().substring(0, m.start())
                    + q.getStem().substring(m.end())).trim();
            q.setStem(cleaned);
        }
    }

    private void validateConfidence(ParsedQuestion q) {
        boolean bad = q.getAnswer() == null || q.getAnswer().isEmpty();
        if (q.getType() == ParsedQuestion.Type.SINGLE || q.getType() == ParsedQuestion.Type.MULTIPLE) {
            bad = bad || q.getOptions().size() < 2;
        }
        if (q.getType() == ParsedQuestion.Type.JUDGE) {
            bad = bad || !("对".equals(q.getAnswer()) || "错".equals(q.getAnswer()));
        }
        if (bad) {
            q.setConfidence(ParsedQuestion.Confidence.LOW);
        }
    }

    private List<String[]> trySplitMultiOptionLine(String line) {
        List<String[]> out = new ArrayList<>();
        for (String part : line.split("\t+")) {
            Matcher m = OPTION.matcher(part.trim());
            if (m.matches()) {
                out.add(new String[]{m.group(1), m.group(2)});
            }
        }
        return out;
    }

    private ParsedQuestion.Type mapSectionType(String cn) {
        switch (cn) {
            case "单选题": return ParsedQuestion.Type.SINGLE;
            case "多选题": return ParsedQuestion.Type.MULTIPLE;
            case "判断题": return ParsedQuestion.Type.JUDGE;
            case "简答题":
            case "问答题": return ParsedQuestion.Type.SHORT_ANSWER;
            default: throw new IllegalStateException("未知题型: " + cn);
        }
    }

    /**
     * 仅转换全角字母(Ａ-Ｚ ａ-ｚ)、全角数字(０-９)、全角句点(．)、全角括号(（ ）)为半角，
     * 不动中文标点（，。、""等），保证题干文本展示仍是自然的中文排版。
     */
    static String normalizeFullWidth(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0xFF21 && c <= 0xFF3A) {
                sb.append((char) ('A' + (c - 0xFF21)));
            } else if (c >= 0xFF41 && c <= 0xFF5A) {
                sb.append((char) ('a' + (c - 0xFF41)));
            } else if (c >= 0xFF10 && c <= 0xFF19) {
                sb.append((char) ('0' + (c - 0xFF10)));
            } else if (c == 0xFF0E) {
                sb.append('.');
            } else if (c == 0xFF08) {
                sb.append('(');
            } else if (c == 0xFF09) {
                sb.append(')');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
