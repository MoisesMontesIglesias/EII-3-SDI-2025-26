# sdi2526-entrega2-test-72

## Ejecución

1. Arrancar la aplicación principal con la base reiniciada:

```bash
RESET_DB_ON_START=true npm start
```
Nada más abrir el proyecto, debemos de hacer un npm start para descargar las carpetas generadas (si no se cargasen correctamente,
pero igualmente vienen en el proyecto importadas) y tener en una terminal el ecosistema NPM activo y ahí es cuando podemos ejecutar
y ver completamente como los tests se ejecutan y están correctos

2. Ejecutar las pruebas desde esta carpeta:

```bash
mvn test -DbaseUrl=http://localhost:3000 -Dtest=ReactSeleniumTests
```

Para la suite web:

```bash
mvn test -DbaseUrl=http://localhost:3000 -Dtest=WebFrontendSeleniumTests
```

Por consola, la suite completa puede lanzarse con:

```bash
mvn test -DbaseUrl=http://localhost:3000 -Dtest=AllTests
```

Opcionalmente, se puede cambiar de navegador:

```bash
mvn test -DbaseUrl=http://localhost:3000 -Dtest=ReactSeleniumTests -Dselenium.browser=edge
```
