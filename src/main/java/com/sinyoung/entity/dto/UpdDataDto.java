package com.sinyoung.entity.dto;


import com.sinyoung.enums.UdpCommand;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UpdDataDto {
    private String msgType;
    private UdpCommand cmd;
    private String macAddress;
    private Map<String, Object> data;
    private String requestAt;

    private String result;
    private String resultMsg;
}
