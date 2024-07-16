package com.sinyoung.enums;

import lombok.Getter;

@Getter
public enum PhoneStatus {
    NOTREADY("not-ready"),
    START("start"),
    READY("ready"),
    NOHB("no-heartbeat"),
    STOP("stop");

    private String status;

    PhoneStatus(String status) {
        this.status = status;
    }
}