package com.uniovi.sdi;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ShoppingCartServlet", value = "/AddToShoppingCart")
public class ShoppingCartServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws
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
        if (product != null) {
            addToShoppingCart(cart, product);
        }

        // Retornar la vista con parámetro "selectedItems"
        request.setAttribute("selectedItems", cart);
        getServletContext().getRequestDispatcher("/cart.jsp").forward(request, response);
    }

    private void addToShoppingCart(Map<String, Integer> cart, String productKey){
        if(cart.get(productKey) == null) cart.put(productKey, 1);
        else{
            int productCount = cart.get(productKey);
            cart.put(productKey,productCount+1);
        }
    }

    private String shoppingCartToHtml(Map<String, Integer> cart){
        StringBuilder shoppingCartToHtml = new StringBuilder();

        for (String key:cart.keySet())
            shoppingCartToHtml.append("<p>[").append(key).append("], ").append(cart.get(key)).append("unidades</p>");

        return shoppingCartToHtml.toString();
    }
}
