package cn.iocoder.yudao.module.courtcase.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CourtCaseActionEnum implements ArrayValuable<String> {

    SUBMIT_ASSIGN("SUBMIT_ASSIGN", "提交分单"),
    SUBMIT_REMIND("SUBMIT_REMIND", "提交预提醒"),
    SUBMIT_TODAY_OVERDUE("SUBMIT_TODAY_OVERDUE", "提交当日未还"),
    SUBMIT_FOLLOW_UP("SUBMIT_FOLLOW_UP", "提交追客"),
    TRANSFER_LEGAL("TRANSFER_LEGAL", "移交法务"),
    FILE_LAWSUIT("FILE_LAWSUIT", "进入起诉"),
    ARCHIVE("ARCHIVE", "归档案件");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(CourtCaseActionEnum::getAction)
            .toArray(String[]::new);

    private final String action;
    private final String name;

    public static CourtCaseActionEnum valueOfAction(String action) {
        return ArrayUtil.firstMatch(item -> item.getAction().equals(action), values());
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
