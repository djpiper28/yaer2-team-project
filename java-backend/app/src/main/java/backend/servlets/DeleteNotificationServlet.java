package backend.servlets;

import backend.exceptions.InvalidNonceException;
import backend.model.Notification;
import backend.model.UserType;
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
import java.util.UUID;

/**
 * Allows the user to place an order
 *
 * @author Danny, Flynn
 * @version 1
 */
public class DeleteNotificationServlet extends HTTPServletOverride {

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

        try {
            final String input = Router.readData(req);
            final JSONObject object = (JSONObject) JSONValue.parse(input);
            final long nonce = Long.parseLong(String.valueOf((String) object.get("nonce")));
            final String jwt = (String) object.get("access-token");
            final String uuid = (String) object.get("notif-uuid");

            if (!r.getUserManager().isAccessValid(jwt, nonce)) {
                throw new InvalidNonceException();
            }

            if (r.getUserManager().getUser(r.getUserManager().getUUIDFromJwt(jwt)).getUserType() == UserType.CUSTOMER) {
                body = Router.getServletError("You are not a waiter.");
                code = 403;
            } else {
                Notification.rmFromDatabase(r.getDatabaseConnector(), UUID.fromString(uuid));

                JSONObject retObj = new JSONObject();
                retObj.put("success", true);

                body = retObj.toJSONString();
            }
        } catch (Exception e) {
            // Bad API Request
            logger.error(e);
            code = 400;
            body = Router.getServletError("Bad request");
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
