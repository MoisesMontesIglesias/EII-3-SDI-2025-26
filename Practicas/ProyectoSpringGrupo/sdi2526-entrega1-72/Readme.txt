Para levantar la base usamos:
Windows:
java -cp "$env:USERPROFILE\.m2\repository\org\hsqldb\hsqldb\2.7.3\hsqldb-2.7.3.jar" org.hsqldb.Server --database.0 file:C:/sdi2526-entrega1-n/data/reservationdb --port 9001

Mac:
java -cp "$HOME/.m2/repository/org/hsqldb/hsqldb/2.7.3/hsqldb-2.7.3.jar" org.hsqldb.server.Server --database.0 file:/Users/ikramelmabroukmorhnane/Desktop/sdi2526-entrega1-72/data/reservationdb --port 9001