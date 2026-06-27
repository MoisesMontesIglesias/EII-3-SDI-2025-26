<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="com.uniovi.sdi.*,java.util.List" %>

<html lang="en">
<head>
    <title>Servlets</title>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB"
          crossorigin="anonymous">
    <script src=https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js integrity="sha384
FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
</head>
<body>
<jsp:useBean id="counter" class="com.uniovi.sdi.Counter" scope="application"/>
<jsp:setProperty name="counter" property="increase" value="1"/>

<!-- Barra de Navegación superior -->
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="collapse navbar-collapse" id="my-navbarColor02">
        <ul class="navbar-nav me-auto">
            <li class="nav-item">
                <a class="nav-link active" aria-current="page" href="AddToShoppingCart">
                    Carrito
                    <span class="visually-hidden">(current)</span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="login.jsp">Login</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="admin.jsp">Administrar productos</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="products">Ver productos</a>
            </li>
        </ul>
        <!-- Alineado a la derecha -->
        <div class="text-white ms-auto">
            <%--<%=counter%> Visitas--%>
            <jsp:getProperty name="counter" property="total"/> Visitas
        </div>
    </div>
</nav>

<!-- Contenido -->
<div class="container" id="main-container">
    <h2>Productos</h2>
    <div class="row ">
        <jsp:useBean id="productsService" class="com.uniovi.sdi.ProductsService"/>
        <c:forEach var="product" begin="0" items="${productsService.products}">
        <div class="col-xs-12 col-sm-6 col-md-4 col-lg-3">
            <div>
                <img src="<c:out value="${product.image}"/>"  alt=""/>
                <div><c:out value="${product.name}"/></div>
                <a href="AddToShoppingCart?product=<c:out value="${product.name}"/>"
                   class="btn btn-default">
                    <c:out value="${product.price}"/> €
                </a>
            </div>
        </div>
        </c:forEach>
    </div>
</div>
</body>
</html>