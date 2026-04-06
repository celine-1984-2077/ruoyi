package cn.iocoder.yudao.module.courtcase.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CourtCaseFilingStatusEnum implements ArrayValuable<String> {

    PENDING("PENDING", "待立案"),
    FILED("FILED", "已立案"),
    REJECTED("REJECTED", "驳回"),
    CLOSED("CLOSED", "结案");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(CourtCaseFilingStatusEnum::getStatus)
            .toArray(String[]::new);

    private final String status;
    private final String name;

    public static CourtCaseFilingStatusEnum valueOfStatus(String status) {
        return ArrayUtil.firstMatch(item -> item.getStatus().equals(status), values());
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
