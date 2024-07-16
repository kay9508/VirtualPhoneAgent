package com.sinyoung.nettyHandler;


import com.google.gson.Gson;
import com.sinyoung.command.ManagementCommand;
import com.sinyoung.entity.Message;
import com.sinyoung.entity.dto.UpdDataDto;
import com.sinyoung.enums.PhoneStatus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.sinyoung.service.RabbitmqProducer;
import com.sinyoung.service.RedisService;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link MessageHandler} is the UDP Message Handler and reply the client after message parsing.
 * @author Sameer Narkhede See <a href="https://narkhedesam.com">https://narkhedesam.com</a>
 * @since Sept 2020
 * 
 */
@Slf4j
@Component
public class MessageHandler extends SimpleChannelInboundHandler<Message> {
	private final RedisService redisService;

	private final RabbitmqProducer rabbitmqProducer;
	private final ManagementCommand managementCommand;

	public MessageHandler(RedisService redisService, RabbitmqProducer rabbitmqProducer, ManagementCommand managementCommand) {
		this.redisService = redisService;
		this.rabbitmqProducer = rabbitmqProducer;
		this.managementCommand = managementCommand;
	}

	// 예외가 발생할 때 동작할 코드를 정의 합니다.
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		cause.printStackTrace();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

		handleMessage(ctx, msg);

	}

	/**
	 * Actual Message handling and reply to server.
	 * 
	 * @param ctx  {@link ChannelHandlerContext}
	 * @param msg  {@link Message}
	 */
	private void handleMessage(ChannelHandlerContext ctx, Message msg) {

		log.info("Message Received : " + msg.getMessage());

		Gson gson = new Gson();
		UpdDataDto response = gson.fromJson(msg.getMessage(), UpdDataDto.class);

		switch (response.getCmd()) {
			case provision:
				log.info("*** Provision Command ***");
				redisService.saveHash(response.getMacAddress(), "port", msg.getSender().getPort() + "");
				redisService.saveHash(response.getMacAddress(), "cmd", response.getCmd().name());
				redisService.saveHash(response.getMacAddress(), "data", response.getData().toString());
				redisService.saveHash(response.getMacAddress(), "time", response.getRequestAt());
				//전달받은 Config값 management 로 보내서 DB에 저장
				Map<String, Object> configDataMap = new HashMap<>();
				if (response.getData().get("data") != null) {
					String configData = gson.toJson(response.getData().get("data"));
					configDataMap = gson.fromJson(configData, Map.class);
				}
				managementCommand.setConfig(response.getMacAddress(), configDataMap, PhoneStatus.START);
				break;
			case registration:
				log.info("registration Command");
				/*
				{"macAddress":"02:42:ac:11:00:99","msgType":"v2a","data":{"reason":"401","status":"FAIL"},"cmd":"registration","requestAt":"20240703185343"}
				*/
				if ("OK".equals(response.getData().get("status"))) {
					managementCommand.updateStatus(response.getMacAddress(), PhoneStatus.READY);
				}
				break;
			case heartbeat:
				if (!redisService.hasHash(response.getMacAddress(), "port")) {
					redisService.saveHash(response.getMacAddress(), "port", msg.getSender().getPort() + "");
				}
				log.info("heartbeat Command");
				break;
			case log_level1:
				log.info("log_level1 Command");
				rabbitmqProducer.sendLog(msg.getMessage());
				break;
			case log_level2:
				log.info("log_level2 Command");
				rabbitmqProducer.sendLogLevel2(msg.getMessage());
				break;
			default:
				log.warn("Unknown Command :{}", response.getCmd());
				break;
		}

		//ByteBuf buf = Unpooled.wrappedBuffer("Hey Sameer Here!!!!".getBytes());

		// Send reply
		final WriteListener listener = new WriteListener() {
			@Override
			public void messageRespond(boolean success) {
				//System.out.println(success ? "reply success" : "reply fail");
				if (success) {
					log.info("Reply Success");
				} else {
					log.info("Reply Fail");
				}
			}
		};

		/*ctx.channel().writeAndFlush(new DatagramPacket(buf, msg.getSender())).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (listener != null) {
					listener.messageRespond(future.isSuccess());
				}
			}
		});*/
	}

	/**
	 * {@link WriteListener} is the lister message status interface.
	 * @author Sameer Narkhede See <a href="https://narkhedesam.com">https://narkhedesam.com</a>
	 * @since Sept 2020
	 * 
	 */
	public interface WriteListener {
		void messageRespond(boolean success);
	}

}
