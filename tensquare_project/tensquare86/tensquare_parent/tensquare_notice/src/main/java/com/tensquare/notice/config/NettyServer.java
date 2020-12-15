package com.tensquare.notice.config;

import com.tensquare.notice.netty.handler.MyWebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * @Description:
 * @Author: yp
 */
public class NettyServer {
    /**
     * 开启NettyServer
     * @param port
     */
    public void start(int port) {
        System.out.println("准备启动Netty...");
        //1.创建ServerBootstrap
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        //2.创建事件轮询租
        //2.1用来处理新连接的
        EventLoopGroup boos = new NioEventLoopGroup();
        //2.2用来处理业务逻辑的，读写
        EventLoopGroup worker = new NioEventLoopGroup();

        serverBootstrap.group(boos, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        //3.设置pipeline
                        //3.1 请求消息解码器pipeline
                        channel.pipeline().addLast(new HttpServerCodec());
                        //3.2  将多个消息转换为单一的request或者response对象pipeline
                        channel.pipeline().addLast(new HttpObjectAggregator(65536));
                        //3.3 处理WebSocket的消息事件pipeline
                        channel.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
                        //3.4创建自己的webSocket处理器，就是用来编写业务逻辑的
                        MyWebSocketHandler myWebSocketHandler = new MyWebSocketHandler();
                        channel.pipeline().addLast(myWebSocketHandler);
                    }
                }).bind(port);
    }


}
