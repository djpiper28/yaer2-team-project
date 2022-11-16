package backend.servlets;

import backend.exceptions.InvalidNonceException;
import backend.exceptions.RegistrationException;
import backend.model.User;
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

/**
 * Servlet to allow users to register for an account.
 *
 * @author Danny
 * @since 1
 */
public class RegisterServlet extends HTTPServletOverride {

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

        logger.info(String.format("A register request from %s", req.getRemoteAddr()));

        final String input = Router.readData(req);
        System.err.println(input);
        final JSONObject object = (JSONObject) JSONValue.parse(input);
        try {
            final long nonce = Long.parseLong((String) object.get("nonce"));
            final String email = (String) object.get("email");
            final String phonenumber = (String) object.get("phonenumber");
            final String firstname = (String) object.get("firstname");
            final String surname = (String) object.get("surname");
            final String password = (String) object.get("password");

            try {
                final User user = User.createNewCustomer(firstname, surname, email, phonenumber);
                final String refreshToken = r.getUserManager().register(user, password, nonce);
                final JSONObject output = new JSONObject();
                output.put("refresh-token", refreshToken);
                body = output.toJSONString();
            } catch (InvalidNonceException | NoSuchAlgorithmException | RegistrationException e) {
                logger.error(e);
                code = 500;
                body = Router.getServletError("Registration failed");
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
