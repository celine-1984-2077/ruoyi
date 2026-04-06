package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CourtCaseDownloadRespVO {

    private String fileName;

    private byte[] content;
}
