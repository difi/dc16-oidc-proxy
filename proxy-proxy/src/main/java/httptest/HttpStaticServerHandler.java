package httptest;

/**
 * Created with IntelliJ IDEA.
 * User: I
 * Date: 07.07.13
 * Time: 0:45
 * To change this template use File | Settings | File Templates.
 */

import cookies.Cookie;
import httptest.Model.ChannelInfo;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class HttpStaticServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final long MEGABYTE = 1024L * 1024L;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        ChannelTrafficShapingHandler trafficHandler = (ChannelTrafficShapingHandler) ctx.pipeline().get("channelTraffic");
        TrafficCounter trafficCounter = trafficHandler.trafficCounter();

        if (!request.getDecoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        if (request.getMethod() != GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        sendInfo(ctx);

        synchronized (HttpStaticServer.channelsInfo) {
            HttpStaticServer.channelsInfo.add(new ChannelInfo(request.getUri(),
                    trafficCounter.cumulativeWrittenBytes(), trafficCounter.cumulativeReadBytes(), (InetSocketAddress)ctx.channel().remoteAddress()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static void sendInfo(ChannelHandlerContext ctx) {
        Cookie c = new Cookie(new UUID(123, 123).toString(), "IDPortenProxy");
        Cookie co = new Cookie(new UUID(1233234245674346723l, 1223).toString(), "DIFITest");
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        c.insertCookieIntoHeader(response);
        co.insertCookieIntoHeader(response);
        StringBuilder buf = new StringBuilder();

        Calendar timeNow = Calendar.getInstance();

        long secondsFromStart = (timeNow.getTimeInMillis()
                - HttpStaticServer.getServerStartTime().getTimeInMillis()) / 1000;

        long[] globalTraff = new long[2];

        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append("Server traffic information");
        buf.append("</title></head>\r\n");
        buf.append("<body><table width='80%' border='1' cellspacing='0' cellpadding='4'>");
        buf.append("<tr><th>Client IP</th><th>Sent/Received (bytes)</th><th>GET query</th></tr>");

        synchronized (HttpStaticServer.channelsInfo) {
            for(ChannelInfo channelInfo: HttpStaticServer.channelsInfo) {
                globalTraff[0] += channelInfo.getWrittenBytes();
                globalTraff[1] += channelInfo.getReadBytes();

                String ip = channelInfo.getClientIp().getHostString();

                buf.append("<tr><td>");
                buf.append(ip);
                buf.append("</td><td>");
                buf.append(channelInfo.getWrittenBytes() + "/" + channelInfo.getReadBytes());
                buf.append("</td><td>");
                buf.append("http://localhost:" + HttpStaticServer.getPort() + channelInfo.getUrl());
                buf.append("</td></tr>");
            }
        }

        int size = HttpStaticServer.channelsInfo.size();
        double totalTraff = globalTraff[0] + globalTraff[1];

        double traff = 0;
        if (secondsFromStart > 0){
            if (size != 0) {
                traff = (totalTraff / size / 1024);
                traff = (double) Math.round(traff * 10.0) / 10.0;
                size /= secondsFromStart;
            }
            if (totalTraff != 0){
                totalTraff = totalTraff / MEGABYTE / secondsFromStart;
                totalTraff = (double) Math.round(totalTraff * 10.0) / 10.0;
            }
        }
        buf.append("<p>" + size + "  requests/sec - ");
        buf.append(totalTraff + " MB/second - ");
        buf.append(traff + " kB/request </p>");

        buf.append("</table></body></html>");

        response.content().writeBytes(Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8));
        System.out.println(response);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}