package cn.iocoder.yudao.module.courtcase.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CourtCaseStageEnum implements ArrayValuable<String> {

    IMPORT("IMPORT", "导入/建档"),
    ASSIGN("ASSIGN", "分单"),
    REMIND("REMIND", "预提醒"),
    TODAY_OVERDUE("TODAY_OVERDUE", "当日未还"),
    FOLLOW_UP("FOLLOW_UP", "分审追客"),
    LEGAL("LEGAL", "法务"),
    LITIGATION("LITIGATION", "起诉/法院处理中"),
    ARCHIVED("ARCHIVED", "归档");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(CourtCaseStageEnum::getStage)
            .toArray(String[]::new);

    private final String stage;
    private final String name;

    public static CourtCaseStageEnum valueOfStage(String stage) {
        return ArrayUtil.firstMatch(item -> item.getStage().equals(stage), values());
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
