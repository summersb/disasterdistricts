# Introduction #

This page will walk you through the steps of installing and running the tool

# Details #

The code has been updated to run under jboss version 7.1.1

You need to configure two modules and a datasource.

Depending on which database you select you must configure the module for the jdbc drivers

Mysql
create ${jboss.home}/modules/com/mysql/jdbc/main
Inside of main copy the mysql jdbc jar file (I have tested with 5.1.23)
Create the module.xml file and add the following
```
<module xmlns="urn:jboss:module:1.1" name="com.mysql.jdbc">
    <resources>
        <resource-root path="mysql-connector-java-5.1.23-bin.jar"/>
    </resources>
    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>
```
Add the driver to the standalone.xml file in ${jboss.home}/standalone/configuration
Locate the 

&lt;datasources&gt;

 section and add this to 

&lt;drivers&gt;


```
<driver name="mysql" module="com.mysql.jdbc"/>
```

Postgres TODO
create ${jboss.home}/modules/org/postgresql/jdbc/main
Inside of main copy the mysql jdbc jar file ()
Create the module.xml file and add the following

Add the driver to the standalone.xml file in ${jboss.home}/standalone/configuration
Locate the 

&lt;datasources&gt;

 section and add this to 

&lt;drivers&gt;


```
```

Now you need to create the module for openjpa
create ${jboss.home}/modules/org/apache/openjpa/main
Inside of main copy openjpa-2.2.2.jar and serp-1.14.1.jar
Create the module.xml file and add the following
```
<module xmlns="urn:jboss:module:1.1" name="org.apache.openjpa">
   <resources>
     <resource-root path="openjpa-2.2.2.jar"/>
     <resource-root path="serp-1.14.1.jar"/>
   </resources>
   <dependencies>
     <module name="javax.persistence.api"/>
     <module name="javax.transaction.api"/>
     <module name="javax.validation.api"/>
     <module name="org.apache.commons.lang"/>
     <module name="org.apache.commons.collections"/>
     <module name="org.apache.log4j"/>
   </dependencies>
 </module>
```

Now start jboss in standalone mode.
You will need to add an admin user if you have not done so.
Open the admin page http://localhost:9990
Click Profile (top right)
Click Datasources (middle left, under connector)
Click Add (on right)
Enter any name but enter java:/jboss/datasources/disaster for the JNDI name
Click next
Select the correct driver
Click next
Enter the connection url (ie jdbc:mysql://localhost:3306/disaster)
Enter the username and password
Click Done
Select the datasource, and click enable

Now deploy the war