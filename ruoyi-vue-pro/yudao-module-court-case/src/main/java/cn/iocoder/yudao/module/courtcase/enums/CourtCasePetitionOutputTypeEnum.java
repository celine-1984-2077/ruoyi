package cn.iocoder.yudao.module.courtcase.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CourtCasePetitionOutputTypeEnum implements ArrayValuable<String> {

    WORD("WORD", "Word"),
    PDF("PDF", "PDF");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(CourtCasePetitionOutputTypeEnum::getType)
            .toArray(String[]::new);

    private final String type;
    private final String name;

    public static CourtCasePetitionOutputTypeEnum valueOfType(String type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
