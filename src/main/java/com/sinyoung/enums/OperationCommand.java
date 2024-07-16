package com.sinyoung.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperationCommand {
    start("생성/시작"), // start
    restart("재실행"), // kill + start
    stop(" 정지"), // kill
    delete("삭제"), // kill + com.sinyoung.config 삭제
    init("초기화"); // kill + com.sinyoung.config 삭제 +start

    private String name;
}
