package backend.servlets;

import backend.exceptions.InvalidNonceException;
import backend.model.MenuItem;
import backend.model.Order;
import backend.model.OrderLine;
import org.apache.logging.log4j.LogManager;
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
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Allows user to change the table number of an order
 *
 * @author Flynn, Danny
 * @version 1
 */
public class ModifyOrderItemServlet extends HTTPServletOverride {
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

            final UUID orderid = UUID.fromString((String) object.get("order-id"));
            final long nonce = Long.parseLong((String) object.get("nonce"));
            final String jwt = (String) object.get("access-token");
            if (!r.getUserManager().isAccessValid(jwt, nonce)) {
                throw new InvalidNonceException();
            }

            final JSONArray items = (JSONArray) object.get("order-lines");
            List<OrderLine> orderlines = new LinkedList<>();
            for (Object itemTmp : items) {
                final JSONObject item = (JSONObject) itemTmp;
                final String requests = (String) item.get("special-requests");
                final int quantity = ((Long) item.get("quantity")).intValue();
                final UUID uuid = UUID.fromString((String) ((JSONObject) item.get("menu-item")).get("uuid")); // This looks kinda vile. sorry my bad

                final MenuItem menuItem = MenuItem.fromUUID(uuid, r.getDatabaseConnector());
                final OrderLine orderLine = new OrderLine(menuItem, quantity, requests);
                orderlines.add(orderLine);
            }

            //modify orderline
            Order order = Order.fromUUID(orderid, r.getDatabaseConnector());
            order.updateOrderLine(orderlines.get(0).getMenuItem().getUUID(), orderlines.get(1), r.getDatabaseConnector());

            JSONObject retObj = new JSONObject();
            retObj.put("success", true);
            body = retObj.toJSONString();

        } catch (Exception e) {
            // Bad API Request
            e.printStackTrace();
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
