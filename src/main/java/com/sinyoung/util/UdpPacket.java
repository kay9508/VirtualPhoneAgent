package com.sinyoung.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class UdpPacket {

  public static final byte[] START = {(byte)0x4b, (byte)0x4b};
  public static final byte[] TAIL = {(byte)0xf5};
  public static final byte[] TYPE_DATA = {(byte)0x01};
  public static final byte[] TYPE_ACK_OK = {(byte)0x02};
  public static final byte[] TYPE_ACK_FAIL = {(byte)0x03};

  public static byte[] makeDataPacket(String payload) {
    try {
      ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
      payloadStream.write(TYPE_DATA);
      payloadStream.write(IdGenerator.getUUID().getBytes());
      payloadStream.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")).getBytes());
      payloadStream.write(payload.getBytes());

      byte[] crc = CRCUtil.crc16(payloadStream.toByteArray());
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      outputStream.write(START); //start
      outputStream.write(DataUtil.getIntTo2Byte(payloadStream.size())); //length
      outputStream.write(payloadStream.toByteArray()); //type + mid + reqTime + payload
      outputStream.write(crc);
      outputStream.write(TAIL);

      return outputStream.toByteArray();

    } catch(Exception e) {
      return null;
    }
  }

  public static void decode(byte[] packet) {

    ByteBuffer buffer = ByteBuffer.wrap(packet);
    byte[] start = new byte[2];
    buffer.get(start);
    log.info("start : [{}]", Hex.encodeHexString(start));

    byte[] len = new byte[2];
    buffer.get(len);
    log.info("len : [{}]", Hex.encodeHexString(len));

    byte[] type = new byte[1];
    buffer.get(type);
    log.info("type : [{}]", Hex.encodeHexString(type));

    byte[] mid = new byte[32];
    buffer.get(mid);
    log.info("mid : [{}]", Hex.encodeHexString(mid));

    byte[] reqTime = new byte[14];
    buffer.get(reqTime);
    log.info("reqTime : [{}]", Hex.encodeHexString(reqTime));

    byte[] payload = new byte[DataUtil.get2ByteToInt(len) - type.length - mid.length- reqTime.length];
    buffer.get(payload);
    log.info("payload : [{}]", Hex.encodeHexString(payload));
    String str = new String(payload, StandardCharsets.UTF_8);
    log.info("str : [{}]", str);

    byte[] crc = new byte[2];
    buffer.get(crc);
    log.info("crc : [{}]", Hex.encodeHexString(crc));

    byte[] tail = new byte[1];
    buffer.get(tail);
    log.info("tail : [{}]", Hex.encodeHexString(tail));

  }
}
