<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
    <title>Lista de productos</title>
</head>
<body>
    <h2>Listado de productos (vista producto)</h2>
    <table class="table value striped">
        <thread>
    <tr>
        <th>Imagen</th>
        <th>Nombre</th>
        <th>Precio</th>
        <th>Acción</th>
    </tr>
        </thread>
        <tbody>
        <c:forEach var="product" items="${storeProducts}">
            <tr>
                <td>
                    <img src="<c:out value='${product.image}'/>" alt="<c:out value='${product.name}'/>" style="width: 100px;"/>
                </td>
                <td>
                    <c:out value="${product.name}"/>
                </td>
                <td>
                    <c:out value="${product.price}"/>
                </td>
                <td>
                    <a href="/AddToShoppingCart?product=<c:out value='${product.name}'/>"> Añadir al carrito</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</body>
</html>