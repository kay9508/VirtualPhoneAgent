package com.sinyoung.entity.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ApiModel(description = "로그레벨 2 전송요청")
public class LogLv2StartReq {

    @NotBlank
    @ApiModelProperty(notes = "MAC 주소", required = true)
    private String macAddr;

    @NotBlank
    public String ip;

    @NotBlank
    public String port;

}
