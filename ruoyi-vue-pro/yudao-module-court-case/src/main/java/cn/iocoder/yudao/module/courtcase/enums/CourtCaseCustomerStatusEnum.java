package cn.iocoder.yudao.module.courtcase.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CourtCaseCustomerStatusEnum implements ArrayValuable<String> {

    PENDING_REPAY("PENDING_REPAY", "待还款"),
    REMINDING("REMINDING", "提醒中"),
    OVERDUE("OVERDUE", "已逾期"),
    FOLLOWING("FOLLOWING", "跟进中"),
    TRANSFERRED_TO_LEGAL("TRANSFERRED_TO_LEGAL", "已移交法诉"),
    REPAID("REPAID", "已还款");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(CourtCaseCustomerStatusEnum::getStatus)
            .toArray(String[]::new);

    private final String status;
    private final String name;

    public static CourtCaseCustomerStatusEnum valueOfStatus(String status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
