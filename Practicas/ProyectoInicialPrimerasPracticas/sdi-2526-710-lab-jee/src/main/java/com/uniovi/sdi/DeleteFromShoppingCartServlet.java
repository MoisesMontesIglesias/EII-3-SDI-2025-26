package com.uniovi.sdi;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "DeleteFromShoppingCartServlet", value = "/DeleteFromShoppingCart")
public class DeleteFromShoppingCartServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        super.doGet(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        HttpSession session = request.getSession();

        HashMap<String, Integer> cart =
                (HashMap<String, Integer>) session.getAttribute("cart");

        // No hay carrito, creamos uno y lo insertamos en sesión
        if (cart == null) {
            cart = new HashMap<String, Integer>();
            session.setAttribute("cart", cart);
        }

        String product = request.getParameter("product");
        String method = request.getParameter("_method");
        if (product != null && "DELETE".equalsIgnoreCase(method)) {
            DeleteFromShoppingCartByItem(cart, product);
        }

        // Retornar la vista con parámetro "selectedItems"
        request.setAttribute("selectedItems", cart);
        getServletContext().getRequestDispatcher("/cart.jsp").forward(request, response);

    }

    private void DeleteFromShoppingCartByItem(Map<String, Integer> cart, String productKey){
        if(cart.get(productKey) != null){
            int productCount = cart.get(productKey);
            if(productCount <= 1){
                cart.remove(productKey);
            } else{
                cart.put(productKey, productCount - 1);
            }
        }
    }
}
