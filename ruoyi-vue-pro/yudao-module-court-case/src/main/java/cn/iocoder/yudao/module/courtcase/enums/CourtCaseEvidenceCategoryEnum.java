package cn.iocoder.yudao.module.courtcase.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CourtCaseEvidenceCategoryEnum implements ArrayValuable<String> {

    ID_CARD("ID_CARD", "身份证正反面"),
    LEASE_CONTRACT("LEASE_CONTRACT", "租赁合同"),
    LOGISTICS_DOC("LOGISTICS_DOC", "物流单据"),
    OVERDUE_PROOF("OVERDUE_PROOF", "逾期证明"),
    COMMUNICATION_RECORD("COMMUNICATION_RECORD", "沟通记录"),
    OTHER("OTHER", "其他");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(CourtCaseEvidenceCategoryEnum::getCategory)
            .toArray(String[]::new);

    private final String category;
    private final String name;

    public static CourtCaseEvidenceCategoryEnum valueOfCategory(String category) {
        return ArrayUtil.firstMatch(item -> item.getCategory().equals(category), values());
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
