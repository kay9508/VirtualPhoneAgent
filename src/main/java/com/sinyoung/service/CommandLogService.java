package com.sinyoung.service;

import com.google.gson.Gson;
import com.sinyoung.enums.RequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommandLogService {

    private final RabbitmqProducer rabbitmqProducer;

    public void sendCommandLog(RequestStatus requestStatus, String macAddr, String cmd, String data) {
        Gson gson = new Gson();
        Map<String, Object> logDataMap = new HashMap<>();
        logDataMap.put("msgType", "a2v");
        logDataMap.put("cmd", cmd);
        logDataMap.put("macAddr", macAddr);
        logDataMap.put("data", data);
        logDataMap.put("status", requestStatus.name());
        logDataMap.put("createdAt", LocalDateTime.now());
        rabbitmqProducer.sendCommLog(gson.toJson(logDataMap));
    }
}
