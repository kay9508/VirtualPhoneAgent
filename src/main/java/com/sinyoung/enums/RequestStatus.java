package com.sinyoung.enums;

import lombok.Getter;

@Getter
public enum RequestStatus {
    REQUEST("request"),
    RESPONSE("response");

    private String status;

    RequestStatus(String status) {
        this.status = status;
    }
}
