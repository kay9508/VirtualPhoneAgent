package com.sinyoung.nettyUdpServer;


import com.sinyoung.command.ManagementCommand;
import com.sinyoung.decoder.MessageDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import com.sinyoung.nettyHandler.MessageHandler;
import org.springframework.stereotype.Component;
import com.sinyoung.service.RabbitmqProducer;
import com.sinyoung.service.RedisService;


/**
 * {@link MessagePipelineFactory} is the pipeline factory type class.
 * @author Sameer Narkhede See <a href="https://narkhedesam.com">https://narkhedesam.com</a>
 * @since Sept 2020
 * 
 */
@Component
public class MessagePipelineFactory implements PipelineFactory{
	private final int availableProcessors;
    private final EventExecutorGroup executors;
    private final RedisService redisService;
    private final RabbitmqProducer rabbitmqProducer;
    private final ManagementCommand managementCommand;

    /**
     * Constructor fott {@link MessagePipelineFactory}
     */
    public MessagePipelineFactory(RedisService redisService, RabbitmqProducer rabbitmqProducer, ManagementCommand managementCommand) {
        availableProcessors = Runtime.getRuntime().availableProcessors();
        executors = new DefaultEventExecutorGroup(availableProcessors);
		this.redisService = redisService;
		this.rabbitmqProducer = rabbitmqProducer;
		this.managementCommand = managementCommand;
    }
    
	/**
	 *	Pipeline Factory method for channel initialization
	 */
	@Override
	public ChannelInitializer<DatagramChannel> createInitializer() {
		
		return new ChannelInitializer<DatagramChannel>() {

			@Override
			protected void initChannel(DatagramChannel ch) throws Exception {
				// Create chanel pipeline
            	ChannelPipeline pipeline = ch.pipeline();
            	
            	final MessageDecoder decoder = new MessageDecoder();

				// 파이프라인 형성
            	pipeline.addLast("decoder", decoder);
            	
            	final MessageHandler handler = new MessageHandler(redisService, rabbitmqProducer, managementCommand);
            	
            	pipeline.addLast(executors, "handler", handler);
				
			}
			
		};
	}

	
	
}
