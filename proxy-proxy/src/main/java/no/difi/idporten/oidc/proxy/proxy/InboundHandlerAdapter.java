package no.difi.idporten.oidc.proxy.proxy;

import com.google.inject.Inject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import no.difi.idporten.oidc.proxy.api.IdentityProvider;
import no.difi.idporten.oidc.proxy.model.ProxyCookie;
import no.difi.idporten.oidc.proxy.api.SecurityConfigProvider;
import no.difi.idporten.oidc.proxy.model.SecurityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * Handler for incoming requests. This handler creates the channel which connects to a outbound server.
 */
public class InboundHandlerAdapter extends AbstractHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(InboundHandlerAdapter.class);

    private volatile Channel outboundChannel;

    private SecurityConfigProvider securityConfigProvider;

    private ProxyCookie proxyCookie;

    @Inject
    public InboundHandlerAdapter(SecurityConfigProvider securityConfigProvider) {
        this.securityConfigProvider = securityConfigProvider;
    }

    /**
     * Only activates the channel and starts the first incoming read which is necessary.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug(String.format("Activating source handler channel %s", ctx.channel()));
        ctx.read();
    }

    private boolean redirectedFromIdp(String path) {
        return (path.contains("?code="));
    }


    /**
     * Bootstraps the backend channel which is the one connected to the outbound server. If the connection is
     * successful, it writes the first message to the outbound server and starts reading the first response back to the
     * source client.
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest httpRequest) throws Exception {
        logger.debug("Handle HTTP request '{}{}'", httpRequest.headers().getAsString(HttpHeaderNames.HOST), httpRequest.uri());

        String path = httpRequest.uri();
        String trimmedPath = path.contains("?") ? path.split("\\?")[0] : path;
        String host = httpRequest.headers().getAsString(HttpHeaderNames.HOST);
        ResponseGenerator responseGenerator = new ResponseGenerator();
        Optional<SecurityConfig> securityConfigOptional = securityConfigProvider.getConfig(host, path);

        try {
            logger.info("Request on: {}{}", httpRequest.headers().getAsString(HttpHeaderNames.HOST), path);

            if (securityConfigOptional.isPresent()) {
                SecurityConfig securityConfig = securityConfigOptional.get();
                String idpName = securityConfig.getIdp();
                CookieHandler cookieHandler = new CookieHandler(securityConfig.getCookieConfig(), host, securityConfig.getPreferredIdpData());
                Optional<ProxyCookie> validProxyCookieOptional = cookieHandler.getValidProxyCookie(httpRequest, securityConfig.getSalt(), httpRequest.headers().get("User-Agent"));

                logger.debug("Preferred IDPs of request: {}"+securityConfig.getPreferredIdpData());
                logger.debug("Has security config: {}", securityConfig);

                if (securityConfig.isSecured() && !securityConfig.isTotallyUnsecured(path)) {
                    Optional<IdentityProvider> idpOptional = securityConfig.createIdentityProvider();

                    boolean requestsLogout = path.endsWith(securityConfig.getLogoutPostUri());
                    logger.debug("{}{} is secured", host, path);

                    if (validProxyCookieOptional.isPresent()) {
                        logger.debug("Has valid ProxyCookie {}", proxyCookie);
                        proxyCookie = validProxyCookieOptional.get();

                        // User has requested logout
                        if (requestsLogout) {
                            logger.info("User has valid cookie and has requested logout from a secured path ({})", proxyCookie);
                            cookieHandler.removeCookie(proxyCookie.getUuid());
                            logger.info("Cookie deleted. Redirecting user to {}", securityConfig.getLogoutRedirectUri());
                            responseGenerator.generateLogoutResponse(ctx, securityConfig);
                            return;
                        }

                        // checks if the information in this cookie is enough for what the request needs
                        // handle login with same cookie on multiple IDPs here
                        boolean cookieHasEnoughInformation = securityConfig.getUserDataNames().stream()
                                .allMatch(userDataName -> proxyCookie.getUserData().containsKey(userDataName));
                        boolean hasNeededSecurity = proxyCookie.getSecurity() >= securityConfig.getSecurity();

                        logger.debug("Request-cookie has security {} and IDP needs security {}", proxyCookie.getSecurity(), securityConfig.getSecurity());

                        if (cookieHasEnoughInformation && hasNeededSecurity) {
                            logger.debug("Cookie has the correct information required for this path");
                            outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie);
                            return;
                        }
                        logger.debug("Cookie was found but does not have the information required for this path");
                    }
                    if (idpOptional.isPresent()) {
                        IdentityProvider idp = idpOptional.get();
                        logger.debug("Has identity provider: {}", idp);

                        // Checks if user tries to log out without valid cookie. Some browsers (i.e. Safari) send a GET request to
                        // the autocomplete'd URL in the browser, causing the server to delete the cookie before user accesses path
                        if (requestsLogout) {
                            logger.warn("User requested logout, but has no valid cookie (probably already deleted). Redirecting to logout-uri");
                            responseGenerator.generateLogoutResponse(ctx, securityConfig);
                            return;
                        }


                        if (redirectedFromIdp(path)) {
                            logger.debug("TypesafePathConfig contains code: {}", path);

                            Map<String, String> userData = idp.getToken(path).getUserData();

                            // Host's config (falls back to default config if not present)
                            int maxExpiry = securityConfig.getCookieConfig().getMaxExpiry(); // in minutes
                            int touchPeriod = securityConfig.getCookieConfig().getTouchPeriod();  // in minutes
                            int security = securityConfig.getSecurity();

                            logger.debug("Provider @{}{} uses touchPeriod {} and maxExpiry {}", securityConfig.getHostname(), securityConfig.getPath(), touchPeriod, maxExpiry);

                            Optional<String> originalPathOptional = RedirectCookieHandler.findRedirectCookiePath(httpRequest, securityConfig.getSalt(), httpRequest.headers().get("User-Agent"));

                            if (validProxyCookieOptional.isPresent()) {
                                proxyCookie = cookieHandler.generateIdpCookie(validProxyCookieOptional.get().getUuid(), userData, security, touchPeriod, maxExpiry);
                                logger.info("Request contains cookie for another IDP. Generated cookie on same UUID for this IDP ({})", proxyCookie);
                            } else {
                                proxyCookie = cookieHandler.generateCookie(userData, security, touchPeriod, maxExpiry);
                                logger.info("Request contains no cookie. Generated new cookie ({})", proxyCookie);
                            }


                            if (originalPathOptional.isPresent() && validProxyCookieOptional.isPresent()) {
                                logger.debug("Redirecting user to original path of request, generated cookie is already in user's browser");
                                responseGenerator.generateRedirectResponse(ctx, securityConfig, httpRequest, originalPathOptional.get(), null);
                            } else if (originalPathOptional.isPresent() ) {
                                logger.debug("Redirecting user to original path of request and inserting cookie to user's browser");
                                responseGenerator.generateRedirectResponse(ctx, securityConfig, httpRequest, originalPathOptional.get(), proxyCookie);
                            } else {
                                outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie);
                            }

                        } else {
                            responseGenerator.generateRedirectResponse(ctx, idp, securityConfig, httpRequest.uri(), httpRequest);
                        }
                    } else {
                        responseGenerator.generateServerErrorResponse(ctx,
                                String.format("Identity provider is not found for secured area %s%s", host, trimmedPath));
                    }
                } else {
                    logger.debug("TypesafePathConfig is not secured: {}{}", host, path);
                    boolean requestsLogout = path.endsWith(securityConfig.getLogoutPostUri());

                    if (validProxyCookieOptional.isPresent()) {
                        proxyCookie = validProxyCookieOptional.get();
                        logger.debug("Has valid ProxyCookie {}", proxyCookie);

                        if (requestsLogout) {
                            logger.info("User has valid cookie and has requested logout from an unsecured path ({})", proxyCookie);
                            cookieHandler.removeCookie(proxyCookie.getUuid());
                            logger.info("Cookie deleted. Redirecting user to {}", securityConfig.getLogoutRedirectUri());
                            responseGenerator.generateLogoutResponse(ctx, securityConfig);
                            return;
                        }
                    }
                    outboundChannel = responseGenerator.generateProxyResponse(ctx, httpRequest, securityConfig, proxyCookie);
                }
            } else {
                logger.debug("Could not get SecurityConfig of host {}", host);
                responseGenerator.generateUnknownHostResponse(ctx, String.format("Host is unconfigured: %s", host));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
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
        logger.debug(String.format("Reading incoming request: %s", msg.getClass()));

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
        } else {
            logger.debug("Outbound Channel Not Active");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Channel Inactive");
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }
}
