package com.sinyoung.entity.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class APIResponse {
	private boolean status; // 성공: true, 실패: false
	private String message; // 성공/실패 메시지
	private Object payload;  // 추가 데이터
}
