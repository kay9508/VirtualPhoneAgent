package com.sinyoung.socket;


import com.sinyoung.command.ManagementCommand;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.sinyoung.nettyUdpServer.MessagePipelineFactory;
import com.sinyoung.nettyUdpServer.NettyServer;
import org.springframework.stereotype.Component;
import com.sinyoung.service.RabbitmqProducer;
import com.sinyoung.service.RedisService;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

@Slf4j
@RequiredArgsConstructor
@Component
public class NettyServerSocket {
    private final ServerBootstrap serverBootstrap;
    private final InetSocketAddress udpPort;

    //여기는 Bean 등록되어있음
    private final RedisService redisService;
    private final RabbitmqProducer rabbitmqProducer;
    private final ManagementCommand managementCommand;
    private Channel serverChannel;

    public void start() {
        try {

            // ChannelFuture: I/O operation의 결과나 상태를 제공하는 객체
            // 지정한 host, port로 소켓을 바인딩하고 incoming connections을 받도록 준비함
            // ChannelFuture serverChannelFuture = serverBootstrap.bind(udpPort.getPort()).sync();

            /*
            NioEventLoop는 I/O 동작을 다루는 멀티스레드 이벤트 루프
            첫번째 'parent' 그룹은 인커밍 커넥션(incomming connection)을 액세스.
		    두번째 'child' 그룹은 액세스한 커넥션의 트래픽을 처리.
            만들어진 채널에 매핑하고 스레드를 얼마나 사용할지는 EventLoopGroup 구현에 의존
             */
//            EventLoopGroup parentGroup = new NioEventLoopGroup(1);
//            EventLoopGroup childGroup = new NioEventLoopGroup();
            /* 서버 부트스트랩을 생성. 이 클래스는 일종의 헬퍼 클래스 */
            /*serverBootstrap.group(parentGroup, childGroup)
                    // 인커밍 커넥션을 액세스하기 위해 새로운 채널을 객체화 하는 클래스 지정합니다.
                    .channel(NioServerSocketChannel.class)
                    // 상세한 Channel 구현을 위해 옵션을 지정할 수 있습니다.
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 새롭게 액세스된 Channel을 처리합니다.
                    // ChannelInitializer는 특별한 핸들러로 새로운 Channel의
                    // 환경 구성을 도와 주는 것이 목적입니다.
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            ChannelPipeline cp = sc.pipeline();
                            cp.addLast(new TestServerHandler());
                        }
                    });*/

            // 인커밍 커넥션을 액세스하기 위해 바인드하고 시작합니다.
//            ChannelFuture cf = serverBootstrap.bind(udpPort.getPort()).sync();

            // 서버 소켓이 닫힐때까지 대기합니다.
//            cf.channel().closeFuture().sync();
            new NettyServer(new MessagePipelineFactory(redisService, rabbitmqProducer, managementCommand)).startup(udpPort.getPort());

            // 서버 소켓이 닫힐 때까지 기다림
            // serverChannel = serverChannelFuture.channel().closeFuture().sync().channel();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Bean을 제거하기 전에 해야할 작업이 있을 때 설정
    @PreDestroy
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
            serverChannel.parent().closeFuture();
        }
    }
}