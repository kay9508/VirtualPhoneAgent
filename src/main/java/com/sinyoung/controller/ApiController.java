package com.sinyoung.controller;

import com.google.gson.Gson;
import com.sinyoung.entity.request.ConfigGetReq;
import com.sinyoung.entity.request.ConfigSetReq;
import com.sinyoung.entity.request.MacAddressReq;
import com.sinyoung.entity.request.StartReq;
import com.sinyoung.entity.response.APIResponse;
import com.sinyoung.enums.OperationCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sinyoung.prod.TopicEventBinder;
import com.sinyoung.service.AgentService;

import javax.validation.Valid;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/agent/api/v1")
@Api(value = "/agent/api/v1")
public class ApiController {

    private final AgentService agentService;
    private final TopicEventBinder topicEventBinder;


    /**
     *  단일 start
     * @param startReq
     * @return
     * @throws UnknownHostException
     */
    @ApiOperation(value = "[ API ->  AGENT]  단일 start", response = APIResponse.class)
    @PostMapping("/start")
    public ResponseEntity start(@Valid StartReq startReq) throws UnknownHostException {
        log.info("-start s => mac:{}, customer:{}, serviceType:{}, heartBeat:{}", startReq.getMacAddr(), startReq.getCustomer(), startReq.getServiceType(), startReq.getHeartBeat());
        //맥주소, //사업자, //SIP서비스명, //요금, //서비스Lv
        boolean result =  agentService.setParamAndRunScript(startReq, OperationCommand.start);
        log.info("-start end => mac:{} result : {}", startReq.getMacAddr(), result);
        return ResponseEntity.ok().body(result);
    }

    /**
     *  엑셀 업로드 (start)
     * @param devicesString
     * @param heartBeat
     * @return
     * @throws UnknownHostException
     */
    @ApiOperation(value = "[ API ->  AGENT]  엑셀 업로드 start", response = APIResponse.class)
    @PostMapping("/startForExcelUpload")
    public ResponseEntity startForExcelUpload(@RequestParam(name = "devicesString") String devicesString,
                                                  @RequestParam(name = "heartBeat") String heartBeat) throws UnknownHostException {
        log.info("---- -excelUpload-start s ---- ");

        List<Boolean> resultList = new ArrayList<>();
        // Json 문자열 -> Student 객체
        Gson gson = new Gson();
        List<Map<String, Object>> StartReqs = gson.fromJson(devicesString, List.class);
        Integer totalCount = StartReqs.size();
        log.info("totalCount : {}", totalCount);
        for (Map temp : StartReqs) {
            String macAddr = (String) temp.get("macAddr");
            String customer = (String) temp.get("customer");
            String serviceType = (String) temp.get("serviceType");
            String serviceLev = String.valueOf(Math.round((Double) temp.get("serviceLev")));
            Boolean feeLev = Boolean.valueOf(temp.get("feeLev").toString());

            StartReq StartReq = new StartReq();
            StartReq.setMacAddr(macAddr);
            StartReq.setCustomer(customer);
            StartReq.setServiceType(serviceType);
            StartReq.setHeartBeat(heartBeat);
            StartReq.setNvoiceLev(serviceLev);
            StartReq.setFeeLev(feeLev);

            Boolean result = agentService.setParamAndRunScript(StartReq, OperationCommand.start);
            resultList.add(result);
        }
        if (resultList.size() == totalCount) {
            log.info("All startForExcelUpload method is success", totalCount);
        } else {
            log.info("Some startForExcelUpload method is failed");
        }

        log.info("---- -excelUpload-start e ----");
        return ResponseEntity.ok().body(true);
    }

    /**
     *  단일 재실행 (restart)
     * @param startReq
     * @return
     * @throws UnknownHostException
     */
    @ApiOperation(value = "[ API ->  AGENT]  단일 재실행", response = APIResponse.class)
    @PostMapping("/restart")
    public ResponseEntity restart(@Valid StartReq startReq) throws UnknownHostException {
        log.info("-restart s => mac:{}, customer:{}, serviceType:{}, heartBeat:{}", startReq.getMacAddr(), startReq.getCustomer(), startReq.getServiceType(), startReq.getHeartBeat());
        //맥주소, //사업자, //SIP서비스명, //요금, //서비스Lv
        boolean result =  agentService.setParamAndRunScript(startReq, OperationCommand.restart);
        log.info("-restart end => mac:{}", startReq.getMacAddr());
        return ResponseEntity.ok().body(result);
    }

    /**
     *  단일 중지 (stop)
     * @param macAddressReq
     * @return
     * @throws UnknownHostException
     */
    @ApiOperation(value = "[ API ->  AGENT]  단일 중지", response = APIResponse.class)
    @PostMapping("/stop")
    public ResponseEntity stop(@Valid MacAddressReq macAddressReq) throws UnknownHostException {
        //맥주소
        log.info("-stop start => mac:{}", macAddressReq.getMacAddr());
        boolean result =  agentService.setParamAndRunScript(macAddressReq, OperationCommand.stop);
        log.info("-stop end => mac:{}", macAddressReq.getMacAddr());
        return ResponseEntity.ok().body(result);
    }

    /**
     *  단일 초기화 (init)
     * @param startReq
     * @return
     * @throws UnknownHostException
     */
    @ApiOperation(value = "[ API ->  AGENT]  단일 초기화", response = APIResponse.class)
    @PostMapping("/init")
    public ResponseEntity init(@Valid StartReq startReq) throws UnknownHostException {
        //맥주소
        log.info("-init start => mac:{}", startReq.getMacAddr());
        boolean result =  agentService.setParamAndRunScript(startReq, OperationCommand.init);
        log.info("-init end => mac:{}", startReq.getMacAddr());
        return ResponseEntity.ok().body(result);
    }

    /**
     *  단일 삭제 (delete)
     * @param macAddressReq
     * @return
     * @throws UnknownHostException
     */
    @ApiOperation(value = "[ API ->  AGENT]  단일 삭제", response = APIResponse.class)
    @DeleteMapping("/delete")
    public ResponseEntity delete(@Valid MacAddressReq macAddressReq) throws UnknownHostException {
        //맥주소
        log.info("-delete start => mac:{}", macAddressReq.getMacAddr());
        boolean result =  agentService.setParamAndRunScript(macAddressReq, OperationCommand.delete);
        log.info("-delete end => mac:{}", macAddressReq.getMacAddr());
        return ResponseEntity.ok().body(result);
    }

    /**
     *   로그레벨 2 전송 요청
     * @param macAddressReq
     * @return
     * @throws IOException
     */
    @ApiOperation(value = "[ API ->  AGENT]  로그레벨 2 전송 요청", response = APIResponse.class)
    @PostMapping("/logLevel2/start")
    public ResponseEntity LogLevel2Start(@Valid MacAddressReq macAddressReq) throws IOException {
        //맥주소
        log.info("-logLevel2Start start => mac:{}", macAddressReq.getMacAddr());
        boolean result =  agentService.logLevel2Start(macAddressReq.getMacAddr());
        log.info("-logLevel2Start end => mac:{}", macAddressReq.getMacAddr());
        return ResponseEntity.ok().body(result);
    }

    /**
     *  로그레벨 2 중지 요청
     * @param macAddressReq
     * @return
     * @throws IOException
     */
    @ApiOperation(value = "[ API ->  AGENT]  로그레벨 2 중지 요청", response = APIResponse.class)
    @PostMapping("/logLevel2/stop")
    public ResponseEntity LogLevel2Stop(@Valid MacAddressReq macAddressReq) throws IOException {
        //맥주소
        log.info("-logLevel2Stop start => mac:{}", macAddressReq.getMacAddr());
        boolean result =  agentService.logLevel2Stop(macAddressReq.getMacAddr());
        log.info("-logLevel2Stop end => mac:{}", macAddressReq.getMacAddr());
        return ResponseEntity.ok().body(result);
    }

    @ApiOperation(value = "[ API ->  AGENT]  config파일 조회", response = APIResponse.class)
    @GetMapping("/config")
    public ResponseEntity getConfig(@Valid ConfigGetReq configGetReq) throws IOException {
        String receivedConfig = agentService.getConfig(configGetReq);
        return ResponseEntity.ok().body(receivedConfig);
    }
    @ApiOperation(value = "[ API ->  AGENT]  config파일 수정", response = APIResponse.class)
    @PutMapping("/config")
    public ResponseEntity setConfig(@Valid ConfigSetReq configSetReq) throws IOException {
        boolean result = agentService.setConfig(configSetReq);
        return ResponseEntity.ok().body(result);
    }

    //TODO rabbitMq 테스트용 (나중에 제거 예정)
    @PostMapping("/send/queues")
    public String sendTest(@RequestBody String message) {
        topicEventBinder.publish(message);
        return "success";
    }
}
