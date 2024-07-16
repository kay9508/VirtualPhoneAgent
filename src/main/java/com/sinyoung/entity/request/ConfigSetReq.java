package com.sinyoung.entity.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigSetReq {
    @NotBlank
    private String macAddr; //	MAC 주소

    private String dataFiledJson;
}
