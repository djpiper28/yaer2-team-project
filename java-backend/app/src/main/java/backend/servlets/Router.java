/*
 * This will route requests to the correct servlets.
 */
package backend.servlets;

import backend.authentication.UserManager;
import backend.database.DatabaseConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * A class to route the api end points to the correct servlets.
 *
 * @author Danny
 * @version 6
 */
public class Router {

    private static final int MAX_THREADS = 100;
    private static final int MIN_THREADS = 10;
    private static final int IDLE_TIMEOUT = 120;

    /**
     * A singleton for the router.
     */
    public static volatile Router router = null;
    private final DatabaseConnector connector;
    private final UserManager userManager;

    /**
     * Private constructor to create the router object and setup the servlets. The router is then started. This method
     * blocks indefinitely.
     *
     * @param url         bind url
     * @param port        bind port
     * @param userManager the user manager for the router
     * @since 6
     */
    private Router(final String url, final int port, final DatabaseConnector connector, final UserManager userManager) throws Exception {
        Router.router = this;
        this.connector = connector;
        this.userManager = userManager;

        QueuedThreadPool threadPool = new QueuedThreadPool(MAX_THREADS, MIN_THREADS, IDLE_TIMEOUT);
        Server server = new Server(threadPool);

        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(port);
        serverConnector.setHost(url);
        serverConnector.setIdleTimeout(1000);
        server.setConnectors(new Connector[]{serverConnector});

        ServletHandler servletHandler = new ServletHandler();

        // Public endpoints
        servletHandler.addServletWithMapping(NotFoundServlet.class, "/"); // Error 404 directs here as well
        this.addEndpoint(servletHandler, NotFoundServlet.class, "/index");
        this.addEndpoint(servletHandler, GetMenuServlet.class, "/api/menu");

        // Authentication and, authorisation
        this.addEndpoint(servletHandler, GetNonceServlet.class, "/api/getnonce");
        this.addEndpoint(servletHandler, RegisterServlet.class, "/api/register");
        this.addEndpoint(servletHandler, LoginServlet.class, "/api/login");
        this.addEndpoint(servletHandler, RefreshServlet.class, "/api/refresh");

        // Order endpoints
        this.addEndpoint(servletHandler, OrderServlet.class, "/api/order");
        this.addEndpoint(servletHandler, TipServlet.class, "/api/tips");
        this.addEndpoint(servletHandler, ViewOrders.class, "/api/vieworders");
        this.addEndpoint(servletHandler, ModifyOrderItemServlet.class, "/api/modifyorderitem");
        this.addEndpoint(servletHandler, ModifyOrderTableNoServlet.class, "/api/modifytableno");
        this.addEndpoint(servletHandler, ViewCustomerOrders.class, "/api/viewcustomerorders");
        this.addEndpoint(servletHandler, OrderStatusServlet.class, "/api/orderstatus");
        this.addEndpoint(servletHandler, NotificationServlet.class, "/api/notify");
        this.addEndpoint(servletHandler, DeleteNotificationServlet.class, "/api/rm-notif");

        server.setHandler(servletHandler);

        server.start();
        server.join();
    }

    /**
        this.addEndpoint(servletHandler, ModifyOrderTableNoServlet.class, "/api/modifytableno");
        this.addEndpoint(servletHandler, ModifyOrderItemServlet.class, "/api/modifyorderitem");
     * Initialises the router router singleton.
     *
     * @param url         url to bind the api server to, usually 127.0.0.1
     * @param port        port to bind the api server to, usually 8009
     * @param connector   the database connector to use
     * @param userManager the user manager of for the router
     * @throws Exception when jetty fails to init, this is the naked exception that jetty throws
     * @since 1
     */
    public static void initRouter(final String url, final int port, final DatabaseConnector connector, final UserManager userManager)
            throws Exception {
        new Router(url, port, connector, userManager); // Set the router singleton up in the constructor
    }

    /**
     * Returns a standard error message for the client that has been sanitised.
     *
     * @param err the error message to send to the client
     * @return the formatted error message
     * @since 2
     */
    public static String getServletError(String err) {
        // Escape json strings
        err = err.replace("\"", "\\");

        return String.format("{\"error\":\"%s\"}", err);
    }

    /**
     * Adds the correct headers to json responses.
     *
     * @param resp response to add the headers to
     * @since 3
     */
    public static void addHeaders(HttpServletResponse resp) {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Headers", "*");
        resp.addHeader("Content-Type", "application/json");
    }

    /**
     * Reads the data of aan http request.
     *
     * @param req the request to read the data from
     * @return the data of the http request
     * @throws IOException thrown when the data cannot be read
     * @since 6
     */
    public static String readData(HttpServletRequest req) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    /**
     * Returns the database connector for the router
     *
     * @return the database connector
     * @since 2
     */
    public DatabaseConnector getDatabaseConnector() {
        return this.connector;
    }

    /**
     * Returns the user manager of the server.
     *
     * @return the server's user manager
     * @since 4
     */
    public UserManager getUserManager() {
        return this.userManager;
    }

    /**
     * Adds an endpoint to the servlet handler.
     *
     * @param s        the servlet handler
     * @param c        the class for the servlet
     * @param endpoint the endpoint relative URL
     * @since 3
     */
    private void addEndpoint(ServletHandler s, Class<? extends javax.servlet.Servlet> c, String endpoint) {
        s.addServletWithMapping(c, endpoint);
        s.addServletWithMapping(c, endpoint + "/");
    }

}
