package cn.iocoder.yudao.module.courtcase.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CourtCaseStatusEnum implements ArrayValuable<Integer> {

    PROCESSING(10, "处理中"),
    CLOSED(20, "关闭"),
    ARCHIVED(30, "已归档"),
    EXCEPTION(40, "异常");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(CourtCaseStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
