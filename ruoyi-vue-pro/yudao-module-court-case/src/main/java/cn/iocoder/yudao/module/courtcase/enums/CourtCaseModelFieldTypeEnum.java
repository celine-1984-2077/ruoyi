package cn.iocoder.yudao.module.courtcase.enums;

public enum CourtCaseModelFieldTypeEnum {

    TEXT,
    DATE,
    NUMBER,
    SINGLE_SELECT,
    MULTI_SELECT;

    public static CourtCaseModelFieldTypeEnum valueOfType(String type) {
        for (CourtCaseModelFieldTypeEnum value : values()) {
            if (value.name().equalsIgnoreCase(type)) {
                return value;
            }
        }
        return null;
    }
}
