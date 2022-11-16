package backend.servlets;

import backend.exceptions.InvalidNonceException;
import backend.exceptions.LoginException;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Servlet to allow users to turn their refresh token into an access token.
 *
 * @author Danny
 * @version 1
 */
public class RefreshServlet extends HTTPServletOverride {

    /**
     * Logger
     *
     * @since 1
     */
    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Router r = Router.router;
        Router.addHeaders(resp);
        String body = "";
        int code = 200;

        logger.info(String.format("An access token refresh request from %s", req.getRemoteAddr()));

        try {
            final String input = Router.readData(req);
            final JSONObject object = (JSONObject) JSONValue.parse(input);
            final long nonce = Long.parseLong(String.valueOf((String) object.get("nonce")));
            final String refreshToken = (String) object.get("refresh-token");

            try {
                final String accessToken = r.getUserManager().useRefresh(refreshToken, nonce);
                final JSONObject output = new JSONObject();
                output.put("access-token", accessToken);
                body = output.toJSONString();
            } catch (InvalidNonceException | LoginException e) {
                //TODO: Log this exception
                e.printStackTrace();
                code = 500;
                body = Router.getServletError("Refreshing the access token failed");
            }
        } catch (Exception e) {
            // Bad API Request
            logger.error(e);
            body = Router.getServletError("Bad request");
            code = 400;
        }

        final ByteBuffer content = ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8));
        final AsyncContext async = req.startAsync();
        final ServletOutputStream out = resp.getOutputStream();
        final int code_final = code;

        out.setWriteListener(new WriteListener() {
            @Override
            public void onWritePossible() throws IOException {
                while (out.isReady()) {
                    if (!content.hasRemaining()) {
                        resp.setStatus(code_final);
                        async.complete();
                        return;
                    }
                    out.write(content.get());
                }
            }

            @Override
            public void onError(Throwable t) {
                getServletContext().log("Async Error", t);
                async.complete();
            }
        });
    }

}
