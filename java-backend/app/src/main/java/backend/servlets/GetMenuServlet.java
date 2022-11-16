package backend.servlets;

import backend.model.MenuItem;
import backend.model.MenuType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

/**
 * Serves the /api/menu endpoint and returns a json array of the menu items.
 *
 * @author Danny
 * @version 1
 */
public class GetMenuServlet extends HTTPServletOverride {

    /**
     * Does the get action.
     *
     * @param req  the request object for the get request
     * @param resp the response object for the get request
     * @throws ServletException not thrown
     * @throws IOException      thrown when a buffer cannot be written to
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Router r = Router.router;
        Router.addHeaders(resp);

        String body = "";
        int code = 200;

        try {
            JSONObject object = new JSONObject();
            List<MenuType> types = MenuType.getMenuTypes(r.getDatabaseConnector());
            JSONArray typesArray = new JSONArray();

            for (MenuType type : types) {
                typesArray.add(type.asJson());
            }
            object.put("types", typesArray);

            List<MenuItem> items = MenuItem.getMenuItems(r.getDatabaseConnector(), true);
            JSONArray array = new JSONArray();

            for (MenuItem item : items) {
                array.add(item.asJson());
            }
            object.put("items", array);

            body = object.toJSONString();
        } catch (SQLException e) {
            e.printStackTrace();
            code = 500;
            body = Router.getServletError("Failed to get menu from database");
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
