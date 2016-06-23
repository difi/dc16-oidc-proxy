package no.difi.idporten.oidc.proxy.proxy;/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * register event handlers in the pipeline
 */
public class TransportFrontendInitializer extends ChannelInitializer<SocketChannel> {
    private static Logger logger = LoggerFactory.getLogger(TransportFrontendHandler.class);

    private int connections;

    public TransportFrontendInitializer(int connections) {
        this.connections = connections;
    }

    /**
     * @param ch
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        logger.debug("Initializing Source channel pool");
        ChannelPipeline p = ch.pipeline();


        p.addLast(new HttpServerCodec(102400, 102400, 102400));
        //   p.addLast(new HttpObjectAggregator(Constants.MAXIMUM_CHUNK_SIZE_AGGREGATOR));
        p.addLast(new HttpRequestHandler(connections));
        p.addLast(new TransportFrontendHandler(connections));
    }
}
