package backend.servlets;

import backend.exceptions.InvalidNonceException;
import backend.model.Order;
import org.json.simple.JSONArray;
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
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Allows user to view all orders associated with a customer
 *
 * @author Danny, Flynn
 * @version 1
 */
public class ViewCustomerOrders extends HTTPServletOverride {
    /**
     * Does the post action.
     *
     * @param req  the request object for the get request
     * @param resp the response object for the get request
     * @throws ServletException not thrown
     * @throws IOException      thrown when a buffer cannot be written to
     */
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
            final UUID custId = r.getUserManager().getUUIDFromJwt(jwt);

            if (!r.getUserManager().isAccessValid(jwt, nonce)) {
                throw new InvalidNonceException();
            }

            List<Order> custOrders = Order.getOrdersFromCustId(r.getDatabaseConnector(), custId, false);
            JSONArray array = new JSONArray();

            for (Order order : custOrders) {
                array.add(order.asJson());
            }
            body = array.toJSONString();
        } catch (SQLException e) {
            e.printStackTrace();
            code = 500;
            body = Router.getServletError("Failed to get order from database");
        } catch (InvalidNonceException e) {
            e.printStackTrace();
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
