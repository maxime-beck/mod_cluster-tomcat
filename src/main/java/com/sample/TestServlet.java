package com.sample;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;

@WebServlet("")
public class TestServlet extends HttpServlet {
    private static final String TEMPLATE = "{\n" +
            "  \"counter\": %d,\n" +
            "  \"id\": \"%s\",\n" +
            "  \"new\": %s,\n" +
            "  \"server\": \"%s\",\n" +
            "  \"hostname\": \"%s\"\n" +
            "}";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession();

        Integer counter = (Integer) session.getAttribute("counter");
        if (counter == null) counter = 0;
        counter++;
        session.setAttribute("counter", counter);

        String id = session.getId();
        String isNew = session.isNew() ? "true" : "false";
        String server = InetAddress.getLocalHost().getHostAddress();
        String hostname = InetAddress.getLocalHost().getHostName();

        try (PrintWriter out = response.getWriter()) {
            out.println(String.format(TEMPLATE, counter, id, isNew, server, hostname));
        }
    }
}