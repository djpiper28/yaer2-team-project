package backend.servlets;

import backend.exceptions.InvalidNonceException;
import backend.exceptions.LoginException;
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
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Servlet to allow user to login and get their refresh token.
 *
 * @author Danny
 * @version 1
 */
public class LoginServlet extends HTTPServletOverride {

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

        logger.info(String.format("A login request from %s", req.getRemoteAddr()));

        try {
            final String input = Router.readData(req);
            final JSONObject object = (JSONObject) JSONValue.parse(input);
            final long nonce = Long.parseLong(String.valueOf((String) object.get("nonce")));
            final String email = (String) object.get("email");
            final String password = (String) object.get("password");
            try {
                final String refreshToken = r.getUserManager().login(email, password, nonce);
                final JSONObject output = new JSONObject();
                final int[] typeCode = {-1};
                r.getDatabaseConnector().runOnDatabase((conn) -> {
                    try {
                        PreparedStatement ps = conn.prepareStatement("select usetype from users where email = ?;");
                        ps.setString(1, email);

                        ResultSet rs = ps.executeQuery();
                        rs.next();

                        typeCode[0] = rs.getInt(1);
                    } catch (SQLException e) {
                        logger.error(e);
                    }
                });

                if (typeCode[0] == -1) {
                    // The user is not in the db - bruh moment
                    logger.error("The user (who we found just now) is not in the database. They must have been deleted between the first check and now.");
                    throw new IllegalStateException("The user no longer exists");
                }

                output.put("user-type", UserType.from(typeCode[0]).toString());
                output.put("refresh-token", refreshToken);
                body = output.toJSONString();
            } catch (InvalidNonceException | LoginException | NoSuchAlgorithmException e) {
                logger.error(e);
                code = 500;
                body = Router.getServletError("Login failed");
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
