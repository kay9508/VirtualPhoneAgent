package com.sinyoung.decoder;

import com.sinyoung.entity.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.sinyoung.util.CRCUtil;
import com.sinyoung.util.DataUtil;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * {@link MessageDecoder} is the Message com.sinyoung.decoder from byte to string using {@link MessageToMessageDecoder}.
 * @author Sameer Narkhede See <a href="https://narkhedesam.com">https://narkhedesam.com</a>
 * @since Sept 2020
 * 
 */
@Slf4j
public class MessageDecoder extends MessageToMessageDecoder<DatagramPacket> {

	public static final byte[] START = {(byte)0x4b, (byte)0x4b};
	public static final byte[] TAIL = {(byte)0xf5};
	public static final byte[] TYPE_DATA_REQUEST = {(byte)0x01};
	public static final byte[] TYPE_DATA_ACK = {(byte)0x02};
	public static final byte[] DATE = "20240508172245".getBytes();
	public Map<String, byte[]> temp;

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) throws Exception {
		JSONObject rtnJsonObject = null;

		InetSocketAddress sender = packet.sender();

		ByteBuf in = packet.content();
		int readableBytes = in.readableBytes();
		if (readableBytes <= 0) {
			return;
		}

		byte[] rcvPktBuf = new byte[readableBytes];
		in.readBytes(rcvPktBuf);

		if (rcvPktBuf[0] != START[0] && rcvPktBuf[1] != START[1]) {
			// TODO 2개로 나눠져 왔을때 처음 받은 패킷이 2번째 패킷일 경우가 있는지 확인 필요
			/*log.error("Invalid tail bytes");
			return;*/
			if (temp == null) {
				temp = new HashMap<>();
			}
			if (temp.get(packet.sender().toString()) == null) {
				temp.put(packet.sender().toString(), rcvPktBuf);
			} else {
				byte[] tempBuf = temp.get(packet.sender().toString());
				int bufLength = tempBuf.length + rcvPktBuf.length;
				byte[] resultBuf = new byte[bufLength];
				for (int i = 0; i < tempBuf.length; i++) {
					resultBuf[i] = tempBuf[i];
				}
				for (int i = 0; i < rcvPktBuf.length; i++) {
					resultBuf[tempBuf.length + i] = rcvPktBuf[i];
				}

				log.info(packet.sender().toString() + "");
				temp.put(packet.sender().toString(), resultBuf);
			}
		}
		if (rcvPktBuf[rcvPktBuf.length - 1] == TAIL[0]) {
			log.info(packet.sender().toString() + ": 마지막 패킷을 받았습니다.");
			// TODO 마지막 패킷이니까 redisDB insert + 플랫폼 서비스로 rest전송

			String bufString = "";
			String bufHexString = "";
			for (int i = 0; i < rcvPktBuf.length; i++) {
				bufString += rcvPktBuf[i] + " ";
				bufHexString += Integer.toString((rcvPktBuf[i] & 0xff) + 0x100, 16).substring(1) + " ";
			}
			// log.debug("inputBuf = " + bufString);
			// log.debug("inputBufHexString = " + bufHexString);

			// 바이트 값 Validation
			if (!validation(rcvPktBuf)) {
				log.error("Invalid packet");
				return;
			}
			// 바이트 값 디코딩
			String bodyString = decode(rcvPktBuf);

			//2. Parser
			JSONParser jsonParser = new JSONParser();

			//3. To Object
			Object obj = jsonParser.parse(bodyString);

			//4. To JsonObject
			rtnJsonObject = (JSONObject) obj;

			Message message = new Message();
			message.setMessage(rtnJsonObject.toJSONString());
			message.setSender(sender);

			out.add(message);

			if (temp != null && temp.get(packet.sender().toString()) != null) {
				temp.remove(packet.sender().toString()); // 임시 저장소에서 삭제
			}
		} else {
			String bufString = "";
			for (int i = 0; i < rcvPktBuf.length; i++) {
				bufString += rcvPktBuf[i] + " ";
			}
			log.error("inputBuf = " + bufString);
		}

		//String msg = in.toString(CharsetUtil.UTF_8);
		//in.readerIndex(in.readerIndex() + in.readableBytes());
	}

	private String decode(byte[] byteValue) {
		int totalLength = byteValue.length;
		int startByteLength = START.length;
		int lenthByteLength = 2;
		int typeByteLength = 1;
		int midByteLength = 32;
		int reqTimeByteLength = 14;
		int bodyStartIndex = startByteLength + lenthByteLength + typeByteLength + midByteLength + reqTimeByteLength;
		int bodyEndIndex = totalLength - 3;
		int crcByteLength = 2;
		int tailByteLength = TAIL.length;

		byte[] body = new byte[bodyEndIndex - bodyStartIndex];
		int idx = 0;
		for (int i = bodyStartIndex; i < bodyEndIndex; i++) {
			body[idx] = byteValue[i];
			idx++;
		}
		String bodyString = new String(body, StandardCharsets.UTF_8);

		return bodyString;
	}

	private boolean validation(byte[] byteValue) {
		/*String bodyString = new String(byteValue, StandardCharsets.UTF_8);
		log.info("bodyString : [{}]", bodyString);*/

		int totalLength = byteValue.length;
		int startByteLength = START.length;
		int lenthByteLength = 2;
		int typeByteLength = 1;
		int midByteLength = 32;
		int reqTimeByteLength = 14;
		int bodyStartIndex = startByteLength + lenthByteLength + typeByteLength + midByteLength + reqTimeByteLength;
		int bodyEndIndex = totalLength - 3;
		int crcByteLength = 2;
		int tailByteLength = TAIL.length;

		byte[] startByte = new byte[]{byteValue[0], byteValue[1]};

		byte[] lengthByte = new byte[]{byteValue[2], byteValue[3]};

		byte[] typeByte = new byte[]{byteValue[4]};

		byte[] midByte = new byte[midByteLength];
		for (int i = 0; i < midByteLength; i++) {
			midByte[i] = byteValue[5 + i];
		}
		String mid = new String(midByte, StandardCharsets.UTF_8);
		// log.info("mid : [{}]", mid);

		byte[] reqTimeByte = new byte[reqTimeByteLength];
		for (int i = 0; i < reqTimeByteLength; i++) {
			reqTimeByte[i] = byteValue[37 + i];
		}
		String reqTime = new String(reqTimeByte, StandardCharsets.UTF_8);
		// log.info("reqTime : [{}]", reqTime);

		byte[] bodyByte = new byte[bodyEndIndex - bodyStartIndex];
		for (int i = bodyStartIndex; i < bodyEndIndex; i++) {
			bodyByte[i - bodyStartIndex] = byteValue[i];
		}

		byte[] crcByte = new byte[crcByteLength];
		crcByte[0] = byteValue[totalLength - 3];
		crcByte[1] = byteValue[totalLength - 2];

		byte[] tailByte = new byte[tailByteLength];
		tailByte[0] = byteValue[totalLength - 1];

		int lengthByteCheck = typeByteLength + midByteLength + reqTimeByteLength + bodyByte.length;

		byte[] checkCrc = new byte[typeByteLength + midByteLength + reqTimeByteLength + bodyByte.length];
		for (int i = 0; i < checkCrc.length; i++) {
			checkCrc[i] = byteValue[4 + i];
		}

		byte[] crc = CRCUtil.crc16(checkCrc);

		if (!Arrays.equals(START, startByte)) {
			log.error("Invalid start bytes  startByte: {}", startByte);
			return false;
		}

		byte[] lengthReverseByte = new byte[]{lengthByte[1], lengthByte[0]};
		if (!(Arrays.equals(lengthByte, DataUtil.getIntTo2Byte(lengthByteCheck)) || Arrays.equals(lengthReverseByte, DataUtil.getIntTo2Byte(lengthByteCheck)))) {
			log.error("lengthByte : {}", lengthByte);
			log.error("Invalid length bytes  DataUtil.getIntTo2Byte(lengthByteCheck): {}, checkCrc.length :{}", DataUtil.getIntTo2Byte(lengthByteCheck), checkCrc.length);
			return false;
		}

		if (!Arrays.equals(typeByte, TYPE_DATA_REQUEST)) {
			log.error("Invalid type bytes typeByte: {}", typeByte);
			return false;
		}

		byte[] crcReverseByte = new byte[]{crcByte[1], crcByte[0]};
		if (!(Arrays.equals(crcByte, crc) || Arrays.equals(crcReverseByte, crc))) {
			log.error("Invalid crc bytes  crc: {}, inputCrcByte: {}", crc, crcByte);
			return false;
		}

		if (!Arrays.equals(tailByte, TAIL)) {
			log.error("Invalid tail bytes tailByte : {}", tailByte);
			return false;
		}

		return true;
	}

}
