package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Inject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.lang.IdentityProviderException;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handler for incoming requests. This handler creates the channel which connects to a outbound server.
 */
public class InboundHandlerAdapter extends AbstractHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(InboundHandlerAdapter.class);

    private volatile Channel outboundChannel;

    private SecurityConfigProvider securityConfigProvider;

    @Inject
    public InboundHandlerAdapter(SecurityConfigProvider securityConfigProvider) {
        this.securityConfigProvider = securityConfigProvider;
    }

    /**
     * Only activates the channel and starts the first incoming read which is necessary.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    private boolean redirectedFromIdp(String path) {
        return (path.contains("?code="));
    }

    private boolean cookieHasEnoughInformation(ProxyCookie proxyCookie, SecurityConfig securityConfig) {
        return securityConfig.getUserDataNames().stream()
                .allMatch(userDataName -> proxyCookie.getUserData().containsKey(userDataName));
    }

    private void handleTotallyUnsecured(ChannelHandlerContext ctx, ResponseGenerator responseGenerator, SecurityConfig securityConfig, HttpRequest httpRequest) {
        outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig);
    }

    private void handleLogout(ChannelHandlerContext ctx, ResponseGenerator responseGenerator, SecurityConfig securityConfig, CookieHandler cookieHandler, HttpRequest httpRequest) {
        Optional<ProxyCookie> proxyCookieOptional = cookieHandler.getValidProxyCookie(httpRequest, securityConfig.getSalt(), httpRequest.headers().getAsString(HttpHeaderNames.USER_AGENT));

        if (proxyCookieOptional.isPresent()) {
            ProxyCookie proxyCookie = proxyCookieOptional.get();
            cookieHandler.removeCookie(proxyCookie.getUuid());
            responseGenerator.generateLogoutResponse(ctx, securityConfig, proxyCookie.getName());
        } else {
            responseGenerator.generateLogoutResponse(ctx, securityConfig, securityConfig.getCookieConfig().getName());
        }

    }

    private void handleIdpLogin(ChannelHandlerContext ctx,
                                ResponseGenerator responseGenerator,
                                SecurityConfig securityConfig,
                                CookieHandler cookieHandler,
                                IdentityProvider identityProvider,
                                HttpRequest httpRequest) throws IdentityProviderException {
        String path = httpRequest.uri();
        String trimmedPath = path.contains("?") ? path.substring(path.indexOf('?')) : path;

        Optional<ProxyCookie> validProxyCookieOptional = cookieHandler.getValidProxyCookie(httpRequest, securityConfig.getSalt(), httpRequest.headers().getAsString(HttpHeaderNames.USER_AGENT));
        ProxyCookie proxyCookie;
        Map<String, String> userData;
        try {
            userData = identityProvider.getToken(path).getUserData();
        } catch (IdentityProviderException e) {
            String uuidForTokenDebugging = UUID.randomUUID().toString();
            logger.warn("UUID for exception: " + uuidForTokenDebugging + ". Gave the following exception: " + e);
            responseGenerator.generateServerErrorResponse(ctx, "Unable to log you in. Code: " + uuidForTokenDebugging);
            return;
        }

        int maxExpiry = securityConfig.getCookieConfig().getMaxExpiry();
        int touchPeriod = securityConfig.getCookieConfig().getTouchPeriod();
        int security = securityConfig.getSecurity();

        Optional<String> originalPathOptional = RedirectCookieHandler.findRedirectCookiePath(httpRequest, securityConfig.getSalt(), httpRequest.headers().getAsString(HttpHeaderNames.USER_AGENT));

        if (validProxyCookieOptional.isPresent()) {
            proxyCookie = cookieHandler.generateIdpCookie(validProxyCookieOptional.get().getUuid(), userData, security, touchPeriod, maxExpiry);
            logger.debug("Request contains cookie for another IDP. Generated cookie on same UUID for this IDP ({})", proxyCookie);
        } else {
            proxyCookie = cookieHandler.generateCookie(userData, security, touchPeriod, maxExpiry);
            logger.debug("Request contains no cookie. Generated new cookie ({})", proxyCookie);
        }

        String redirectPath = originalPathOptional.orElse(trimmedPath);

        responseGenerator.generateRedirectBackToOriginalPathResponse(ctx, securityConfig, httpRequest, redirectPath, proxyCookie);
    }

    private void handleSecured(ChannelHandlerContext ctx,
                               ResponseGenerator responseGenerator,
                               SecurityConfig securityConfig,
                               CookieHandler cookieHandler,
                               IdentityProvider identityProvider,
                               HttpRequest httpRequest) {
        String path = httpRequest.uri();

        Optional<ProxyCookie> validProxyCookieOptional = cookieHandler.getValidProxyCookie(httpRequest, securityConfig.getSalt(), httpRequest.headers().getAsString(HttpHeaderNames.USER_AGENT));
        ProxyCookie proxyCookie;

        if (validProxyCookieOptional.isPresent() && isLoggedIn(validProxyCookieOptional.get(), securityConfig)) {
            proxyCookie = validProxyCookieOptional.get();
            outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie);
        } else if (redirectedFromIdp(path)) {
            try {
                handleIdpLogin(ctx, responseGenerator, securityConfig, cookieHandler, identityProvider, httpRequest);
            } catch (IdentityProviderException exc) {
                logger.warn("Could not get token from {}", securityConfig.getIdp());
                logger.warn(exc.getMessage(), exc);
                responseGenerator.generateRedirectToIdentityProviderResponse(ctx, identityProvider, securityConfig, path, httpRequest);
            }
        } else {
            responseGenerator.generateRedirectToIdentityProviderResponse(ctx, identityProvider, securityConfig, httpRequest.uri(), httpRequest);
        }
    }

    private void handleUnsecured(ChannelHandlerContext ctx,
                                 ResponseGenerator responseGenerator,
                                 SecurityConfig securityConfig,
                                 CookieHandler cookieHandler,
                                 HttpRequest httpRequest) {
        Optional<ProxyCookie> validProxyCookieOptional = cookieHandler.getValidProxyCookie(httpRequest, securityConfig.getSalt(), httpRequest.headers().getAsString(HttpHeaderNames.USER_AGENT));
        ProxyCookie proxyCookie;
        if (validProxyCookieOptional.isPresent()) {
            proxyCookie = validProxyCookieOptional.get();
            outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie);
        } else {
            outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig);
        }
    }

    private boolean isLoggedIn(ProxyCookie proxyCookie, SecurityConfig securityConfig) {
        return cookieHasEnoughInformation(proxyCookie, securityConfig) && proxyCookie.getSecurity() >= securityConfig.getSecurity();
    }

    /**
     * Bootstraps the backend channel which is the one connected to the outbound server. If the connection is
     * successful, it writes the first message to the outbound server and starts reading the first response back to the
     * source client.
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest httpRequest) throws Exception {
        String path = httpRequest.uri();
        String host = httpRequest.headers().getAsString(HttpHeaderNames.HOST);
        ResponseGenerator responseGenerator = new ResponseGenerator();
        Optional<SecurityConfig> securityConfigOptional = securityConfigProvider.getConfig(host, path);

        try {
            if (securityConfigOptional.isPresent()) {
                SecurityConfig securityConfig = securityConfigOptional.get();
                CookieHandler cookieHandler = new CookieHandler(securityConfig.getCookieConfig(), host, securityConfig.getPreferredIdpData());
                Optional<IdentityProvider> identityProviderOptional = securityConfig.createIdentityProvider();

                if (securityConfig.isTotallyUnsecured(path)) {
                    handleTotallyUnsecured(ctx, responseGenerator, securityConfig, httpRequest);
                } else if (securityConfig.isLogoutPath()) {
                    handleLogout(ctx, responseGenerator, securityConfig, cookieHandler, httpRequest);
                } else if (securityConfig.isSecured() && identityProviderOptional.isPresent()) {
                    handleSecured(ctx, responseGenerator, securityConfig, cookieHandler, identityProviderOptional.get(), httpRequest);
                } else {
                    handleUnsecured(ctx, responseGenerator, securityConfig, cookieHandler, httpRequest);
                }
            } else {
                responseGenerator.generateUnknownHostResponse(ctx, String.format("Host is unconfigured: %s", host));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            responseGenerator.generateServerErrorResponse(
                    ctx,
                    String.format("An exception was caught: %s\nPlease check if your configuration is valid", e));
        }
    }

    /**
     * Reading incoming messages from the local client. The first message for a new incoming connection will bootstrap
     * the outbound channel. The next messages will just be written to the outbound channel.
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        } else if (outboundChannel != null && outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }
}
