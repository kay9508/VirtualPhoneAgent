package com.sinyoung.nettyUdpServer;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link NettyServer} is the UDP server class.
 * @author Sameer Narkhede See <a href="https://narkhedesam.com">https://narkhedesam.com</a>
 * @since Sept 2020
 * 
 */
@Slf4j
public class NettyServer {

	private final EventLoopGroup bossLoopGroup;
	
	private final ChannelGroup channelGroup;
	
	private final MessagePipelineFactory messagePipelineFactory;

	/**
	 * Initialize the netty server class
	 * @param pipelineFactory {@link Class} of the piprline factory type
	 */
	public NettyServer(MessagePipelineFactory pipelineFactory) {
		// Initialization private members
		
		this.bossLoopGroup = new NioEventLoopGroup();

		this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

		this.messagePipelineFactory = pipelineFactory;

	}
	
	
	/**
	 * Startup the UDP server
	 * 
	 * @param port port of the server
	 * @throws Exception if any {@link Exception}
	 */
	public final void startup(int port) throws Exception {
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(bossLoopGroup)
        .channel(NioDatagramChannel.class)
        .option(ChannelOption.AUTO_CLOSE, true)
        .option(ChannelOption.SO_BROADCAST, true);

//        PipelineFactory pipelineFactory = (PipelineFactory) messagePipelineFactory.newInstance();
        
        @SuppressWarnings("rawtypes")
		ChannelInitializer initializer = messagePipelineFactory.createInitializer();
        
        bootstrap.handler(initializer);

        try {
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
			log.info("UDP 서버 시작됨: {}", channelFuture.channel().localAddress());

            channelGroup.add(channelFuture.channel());

			channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
			e.printStackTrace();
            shutdown();
            throw e;
        }
    }

    /**
     * Shutdown the server 
 	 * @throws Exception
     */
    public final void shutdown() throws Exception {
        channelGroup.close();
        bossLoopGroup.shutdownGracefully();
    }
	
}
