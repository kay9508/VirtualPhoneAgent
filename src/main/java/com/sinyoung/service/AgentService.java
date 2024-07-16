package com.sinyoung.service;

import com.google.gson.Gson;
import com.sinyoung.command.ManagementCommand;
import com.sinyoung.entity.dto.UpdDataDto;
import com.sinyoung.entity.request.ConfigGetReq;
import com.sinyoung.entity.request.ConfigSetReq;
import com.sinyoung.entity.request.MacAddressReq;
import com.sinyoung.entity.request.StartReq;
import com.sinyoung.enums.OperationCommand;
import com.sinyoung.enums.PhoneStatus;
import com.sinyoung.enums.RequestStatus;
import com.sinyoung.exception.ScriptRunException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sinyoung.util.DataUtil;
import com.sinyoung.util.UdpPacket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgentService {

    private final CommandLogService commandLogService;

    @Value("${script.path}")
    private String SCRIPT_PATH;

    @Value("${start.script.name}")
    private String START;
    @Value("${restart.script.name}")
    private String RESTART;
    @Value("${stop.script.name}")
    private String STOP;
    @Value("${delete.script.name}")
    private String DELETE;
    @Value("${init.script.name}")
    private String INIT;

    @Value("${server.udp.port}")
    private String serverUdpPort;

    private final RedisService redisService;

    private final ManagementCommand managementCommand;

    /**
     * 실행, 재실행, 초기화
     *
     * @param startReq
     * @return
     * @throws ScriptRunException
     */
    @Transactional
    public boolean setParamAndRunScript(StartReq startReq, OperationCommand operationCommand) throws ScriptRunException, UnknownHostException {
        try {
            Gson gson = new Gson();
            commandLogService.sendCommandLog(RequestStatus.REQUEST, startReq.getMacAddr(), operationCommand.name() + " script run", gson.toJson(startReq));

            if (operationCommand == OperationCommand.init) {
                managementCommand.init(startReq.getMacAddr());
                redisService.delete(startReq.getMacAddr());
            }

            InetAddress local = InetAddress.getLocalHost();
            String ip = local.getHostAddress();

            String param = startReq.getMacAddr() + " " + startReq.getCustomer() + " " +
                    startReq.getServiceType() + " " + ip + " " + serverUdpPort + " " + startReq.getHeartBeat();

            // start와 init만 요금레벨과 서비스레벨 파라메터를 추가한다.
            if (operationCommand == OperationCommand.start || operationCommand == OperationCommand.init) {
                param += " " + (startReq.getFeeLev() ? "1" : "0") + " " + startReq.getNvoiceLev();
            }

            runScript(operationCommand, param);

            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 중지, 삭제
     *
     * @param macAddressReq
     * @param operationCommand
     * @return
     * @throws ScriptRunException
     * @throws UnknownHostException
     */
    @Transactional
    public boolean setParamAndRunScript(MacAddressReq macAddressReq, OperationCommand operationCommand) throws ScriptRunException, UnknownHostException {
        try {
            commandLogService.sendCommandLog(RequestStatus.REQUEST, macAddressReq.getMacAddr(), operationCommand.name() + " script run", null);
            String param = macAddressReq.getMacAddr();
            runScript(operationCommand, param);

            switch (operationCommand) {
                case stop:
                    managementCommand.updateStatus(macAddressReq.getMacAddr(), PhoneStatus.STOP);
                    break;
                case delete:
                    redisService.delete(macAddressReq.getMacAddr());
                    break;
            }

            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    private void runScript(OperationCommand oc, String param) throws ScriptRunException {
        String cmd = "";
        switch (oc) {
            case start:
                cmd = SCRIPT_PATH + START + " ";
                break;
            case restart:
                cmd = SCRIPT_PATH + RESTART + " ";
                break;
            case stop:
                cmd = SCRIPT_PATH + STOP + " ";
                break;
            case delete:
                cmd = SCRIPT_PATH + DELETE + " ";
                break;
            case init:
                cmd = SCRIPT_PATH + INIT + " ";
                break;
        }
        
        try {
            // Build the com.sinyoung.command as a list of strings
            log.info("run script : {}", cmd + param);
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", cmd + param);
//            pb.redirectErrorStream(true);

            // Start the process
            Process process = pb.start();

            // Read the output from the process
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            Integer count = 0;
            while (!("".equals((line = reader.readLine())) || line == null)) {
                log.info("response" + count + " : " + line);
                count++;
            }

            // Wait for the process to complete and check the exit value
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                log.error("Command exited with error code: " + exitValue);
                throw new InterruptedException("An error occurred while running the CUSTOM_SCRIPT.");
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new ScriptRunException("An error occurred while running the CUSTOM_SCRIPT.");
        }
    }

    @Transactional
    public boolean logLevel2Start(String macAddr) throws IOException {
        String port = redisService.getHash(macAddr, "port");
        if (port == null) {
            log.error("mac : {} Port is null", macAddr);
            return false;
        } else {
            Gson gson = new Gson();
            commandLogService.sendCommandLog(RequestStatus.REQUEST, macAddr, "Log Level2 Start", null);
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("msgType", "a2v");
            paramMap.put("cmd", "log_level2");
            paramMap.put("status", "ing");
            paramMap.put("macAddress", macAddr);
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("ip", "localhost");
            dataMap.put("port", serverUdpPort);
            dataMap.put("endTime", LocalDateTime.now().plusHours(24).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            paramMap.put("data", "");
            paramMap.put("requestAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            String jsonParamString = gson.toJson(paramMap);

            this.udpSender(port, jsonParamString);
            return true;
        }
    }

    @Transactional
    public String getConfig(ConfigGetReq configGetReq) throws IOException {
        String port = redisService.getHash(configGetReq.getMacAddr(), "port");
        if (port == null) {
            log.error("mac : {} port is null", configGetReq.getMacAddr());
            throw new IllegalArgumentException("mac : " + configGetReq.getMacAddr() + " port is null");
        } else {
            Gson gson = new Gson();
            commandLogService.sendCommandLog(RequestStatus.REQUEST, configGetReq.getMacAddr(), "Config Get", gson.toJson(configGetReq));
            Map<String, String> testMap = new HashMap<>();
            testMap.put("msgType", "a2v");
            testMap.put("cmd", "config_get");
            testMap.put("macAddress", configGetReq.getMacAddr());
            Map<String, String> tempMap2 = new HashMap<>();
            tempMap2.put("sectionName", configGetReq.getSectionName());
            testMap.put("data", gson.toJson(tempMap2));
            testMap.put("requestAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            String jsonParamString = gson.toJson(testMap);

            UpdDataDto result = this.udpSendAndResponse(port, jsonParamString);

            commandLogService.sendCommandLog(RequestStatus.RESPONSE, configGetReq.getMacAddr(), "Config Get", gson.toJson(result.getData()));
            if ("ok".equals(result.getResult())) {
                return gson.toJson(result.getData());
            } else {
                return "";
            }
        }
    }

    @Transactional
    public boolean setConfig(ConfigSetReq configSetReq) throws IOException {
        String port = redisService.getHash(configSetReq.getMacAddr(), "port");
        if (port == null) {
            log.error("mac : {} port is null", configSetReq.getMacAddr());
            throw new IllegalArgumentException("mac : " + configSetReq.getMacAddr() + " port is null");
        } else {
            Gson gson = new Gson();
            commandLogService.sendCommandLog(RequestStatus.REQUEST, configSetReq.getMacAddr(), "Config Set", configSetReq.getDataFiledJson());
            Map<String, String> testMap = new HashMap<>();
            testMap.put("msgType", "a2v");
            testMap.put("cmd", "config_set");
            testMap.put("macAddress", configSetReq.getMacAddr());
            testMap.put("data", configSetReq.getDataFiledJson());
            testMap.put("requestAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            String jsonParamString = gson.toJson(testMap);

            UpdDataDto result = this.udpSendAndResponse(port, jsonParamString);

            commandLogService.sendCommandLog(RequestStatus.RESPONSE, configSetReq.getMacAddr(), "Config Set", gson.toJson(result));

            if ("ok".equals(result.getResult())) {
                managementCommand.setConfig(configSetReq.getMacAddr(), gson.fromJson(configSetReq.getDataFiledJson(), Map.class), null);
                return true;
            } else {
                throw new IllegalArgumentException("Config Set Error");
            }
        }
    }

    @Transactional
    public boolean logLevel2Stop(String macAddr) throws IOException {
        String port = redisService.getHash(macAddr, "port");
        if (port == null) {
            log.error("mac : {} port is null", macAddr);
            return false;
        } else {
            commandLogService.sendCommandLog(RequestStatus.REQUEST, macAddr, "Log Level2 Stop", null);
            Gson gson = new Gson();
            Map<String, String> testMap = new HashMap<>();
            testMap.put("msgType", "a2v");
            testMap.put("cmd", "log_level2");
            testMap.put("status", "stop");
            testMap.put("macAddress", macAddr);
            testMap.put("data", "");
            testMap.put("requestAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            String jsonParamString = gson.toJson(testMap);

            this.udpSender(port, jsonParamString);
            return true;
        }
    }

    private void udpSender(String port, String jsonStringData) throws IOException {
        String targetIP = "localhost";
        int targetPort = Integer.parseInt(port);

        // Convert message to bytes
//        byte[] data = jsonStringData.getBytes();

        byte[] data = UdpPacket.makeDataPacket(jsonStringData);

        log.info("* Send UDP - Port : {} jsonStringData : {}", port, jsonStringData);

        // Create a DatagramPacket to encapsulate the data and destination
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(targetIP), targetPort);

        // Create a DatagramSocket to send the packet
        DatagramSocket socket = new DatagramSocket();

        // Send the packet
        socket.send(packet);

        // Close the com.sinyoung.socket
        socket.close();

    }

    private UpdDataDto udpSendAndResponse(String port, String jsonStringData) throws IOException {
        String targetIP = "localhost";
        int targetPort = Integer.parseInt(port);

        // Convert message to bytes
//        byte[] data = jsonStringData.getBytes();

        byte[] data =UdpPacket.makeDataPacket(jsonStringData);

        String bufHexString = "";
        for (int i = 0; i < data.length; i++) {
            bufHexString += Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1) + " ";
        }
        //log.info("inputBufHexString = " + bufHexString);
        log.info("* Send UDP - Port : {} jsonStringData : {} dataLength:{}", port, jsonStringData, data.length);

        // Create a DatagramPacket to encapsulate the data and destination
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(targetIP), targetPort);

        // Create a DatagramSocket to send the packet
        DatagramSocket socket = new DatagramSocket();

        // Send the packet
        socket.send(packet);

        // 3. 응답 수신
        byte[] buffer = new byte[2048];
        DatagramPacket receivePacket = new DatagramPacket(buffer, 0, buffer.length);
        socket.receive(receivePacket);

        // 4. 수신된 데이터 처리
        /*String responseData = new String(receivePacket.getData(), 0, receivePacket.getLength());
        String responseBytes = "";
        for (byte b : receivePacket.getData()) {
            responseBytes += b + " ";
        }
        log.info("response data : {}", responseBytes);*/

        String bodyString = this.decode(receivePacket.getData());
        log.info("response bodyString : {}", bodyString);

        // Close the com.sinyoung.socket
        socket.close();

        Gson gson = new Gson();
        UpdDataDto updDataDto = gson.fromJson(bodyString, UpdDataDto.class);
        return updDataDto;
    }

    private String decode(byte[] byteValue) {
        final byte[] START = {(byte)0x4b, (byte)0x4b};
        final byte[] TAIL = {(byte)0xf5};

        byte[] lengthByte = new byte[]{byteValue[2], byteValue[3]};
        Integer realLength = DataUtil.get2ByteToInt(lengthByte);
        Integer bobyLength = realLength - 1 - 32 - 14;

        int totalLength = 2 + realLength + 2 + 2 + 1;
        int startByteLength = START.length;
        int lenthByteLength = 2;
        int typeByteLength = 1;
        int midByteLength = 32;
        int reqTimeByteLength = 14;
        int bodyStartIndex = startByteLength + lenthByteLength + typeByteLength + midByteLength + reqTimeByteLength;
        int bodyEndIndex = totalLength - 3;
        // int crcByteLength = 2;
        // int tailByteLength = TAIL.length;
        byte[] body = new byte[bobyLength];
        int idx = 0;
        for (int i = bodyStartIndex; i < bodyEndIndex; i++) {
            body[idx] = byteValue[i];
            idx++;
        }

        String bodyString = new String(body, StandardCharsets.UTF_8);

        return bodyString;
    }
}
