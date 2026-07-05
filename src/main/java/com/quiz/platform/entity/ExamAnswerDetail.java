package com.quiz.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_answer_detail")
public class ExamAnswerDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long questionId;
    private String questionType;
    private String userAnswer;
    /** 正确答案快照 */
    private String correctAnswer;
    /** 1正确 0错误 null待判 */
    private Integer isCorrect;
    private BigDecimal score;
    private LocalDateTime answeredAt;
}
