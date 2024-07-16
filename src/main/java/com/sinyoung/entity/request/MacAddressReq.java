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
@ApiModel(description = "단말 기동 or 중지 or 삭제")
public class MacAddressReq {

    @NotBlank
    @ApiModelProperty(notes = "MAC 주소", required = true)
    private String macAddr;

}
