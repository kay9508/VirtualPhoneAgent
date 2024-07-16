package com.sinyoung.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UdpCommand {
    provision("프로비전 완료 후 agent로 전송"),
    registration("regi 완료 후 agent로 전송"),
    heartbeat("주기적으로 보내는 heartbeat"),
    log_level1("로그레벨 1"),
    log_level2("로그레벨 2"),

    log_level2_start("로그레벨 2 전송 요청"),
    log_level2_stop("로그레벨 2 중지 요청"),
    config_set("com.sinyoung.config 값 변경"),
    config_get("com.sinyoung.config 값 조회");

    private String description;
}
