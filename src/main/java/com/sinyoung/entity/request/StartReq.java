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
public class StartReq {
    private String id; //(PK)	아이디(PK)

    @NotBlank
    private String macAddr; //	MAC 주소

    @NotBlank
    private String customer; //	고객코드

    @NotBlank
    private String serviceType; //	서비스타입(SIP)

    @NotBlank
    private String heartBeat; // 하트비트

    private String nvoiceLev; // 서비스레벨

    private Boolean feeLev; // 요금레벨

}
