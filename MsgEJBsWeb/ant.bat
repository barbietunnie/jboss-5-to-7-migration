@echo off

rem this batch-file can be used to override the java_home or ant_home
rem environment or to set a special classpath and so on...

set ANT_HOME=../../apache-ant-1.7.0

rem set JAVA_HOME=../../jdk1.5.0_12
set JAVA_HOME=../../jdk1.6.0_37

call "%ANT_HOME%\bin\ant" -noclasspath -buildfile build.xml %1 %2 %3 %4 %5 %6 %7 %8 %9
