/*
 * Serves the index endpoint.
 */
package backend.servlets;

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
 * Serves an endpoint not found message.
 *
 * @author Danny
 * @version 2
 */
public class NotFoundServlet extends HTTPServletOverride {

    /**
     * The static string that the index page serves.
     *
     * @since 2
     */
    public static final String INDEX_STATIC = "Endpoint not found!";

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log(req.getRequestURI());
        Router r = Router.router;
        Router.addHeaders(resp);
        ByteBuffer content = ByteBuffer.wrap(INDEX_STATIC.getBytes(StandardCharsets.UTF_8));

        AsyncContext async = req.startAsync();
        ServletOutputStream out = resp.getOutputStream();
        out.setWriteListener(new WriteListener() {
            @Override
            public void onWritePossible() throws IOException {
                while (out.isReady()) {
                    if (!content.hasRemaining()) {
                        resp.setStatus(200);
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
