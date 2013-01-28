Maven Installs 
--------------
1. Download log4jdbc4-1.2alpha2.jar (http://log4jdbc.googlecode.com/files/log4jdbc4-1.2alpha2.jar)
2. Run the following on command line
     mvn install:install-file -Dfile=log4jdbc4-1.2alpha2.jar -DgroupId=net.sf.log4jdbc -DartifactId=log4jdbc4 -Dversion=1.2alpha2 -DgeneratePom=true -Dpackaging=jar
     
mvn install:install-file -Dfile=C:\oraclexe\app\oracle\product\10.2.0\server\jdbc\lib\ojdbc14.jar -DgroupId=ojdbc -DartifactId=ojdbc -Dversion=14 -Dpackaging=jar -DgeneratePom=true     