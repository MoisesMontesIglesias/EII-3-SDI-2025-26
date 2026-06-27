package com.uniovi.sdi;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "GreetingServlet", value = "/GreetingServlet")
public class GreetingServlet extends HttpServlet {
    int contador = 0;
    private final Object mutex = new Object();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<HTML>");
        out.println("<HEAD><TITLE>Hello World!</TITLE></HEAD>");
        out.println("<BODY>");
        String name = request.getParameter("name");
        if (name != null) {
            out.println("Hello " + name + "<br>");
        }
        out.println("</BODY></HTML>");

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            out.println("Se ha producido un error inesperado");
        }
        out.println("Thread ID: "+Thread.currentThread().threadId() +"<br>");
        //contador es un recurso compartido entre todos los hilos.
        //Bloqueamos el acceso a un único hilo a la vez.
        synchronized (mutex)
        {
            contador++;
            out.println("Visits:"+contador+"<br>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        doGet(request, response);
    }
}

