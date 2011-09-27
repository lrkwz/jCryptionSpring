jCryption for Spring
====================

Use this module to implement a client-server JavaScript to Java based encryption system without the need <sic> of SSL.
This library requires the use of jCryption (http://www.jcryption.org) a JavaScript/PHP framework based on jQuery.

The objectives for this springframework integration module are:

* get rid of the PHP component
* do not inject (decouple) decrypting code into the standard mvc controllers

The framework implements the following process:

1. the javascript hook calls the jCryptionSpring server side controller to obtain the encryption public key before the POST
2. the javascript library crypts all the form's parameters into a single string which is POSTed to the standard user defined controller
3. a filter intercepts all the calls which contain the crypted variable and decrypts it to it's original values; the original request is wrapped in order to provide the same parameters back to the standard user defined controller ad if the POST hasn't been crypted at all (100% transparent to yuor code!)
4. from this point on the filter chain is executed as usual

In this way you can build a standard spring mvc application inserting form crypting only where you need it; let's try it step by step using spring roo (not necessary but quite useful even for this small sample):

execute this commands in roo shell in order to quicky startup a web app to create/show/modify a simple UserProfile object

```
project --topLevelPackage org.gitorious.jcryptionspring.sample
persistence setup --database HYPERSONIC_IN_MEMORY --provider HIBERNATE 
entity --class ~domain.UserProfile
field string --class ~domain.UserProfile --fieldName email
field string --class ~domain.UserProfile --fieldName name
field string --class ~domain.UserProfile --fieldName surname
field date --class ~domain.UserProfile --fieldName birthday --type java.util.Calendar
field boolean --class ~domain.UserProfile --fieldName enabled
logging setup --level DEBUG 
controller scaffold --class ~.web.UserProfileController 
security setup
dependency add --artifactId jcryption-spring --groupId org.gitorious.jcryptionspring --version 0.1.2 
quit
```

this script also appends in pom.xml the dependency from the integration module

```XML
<dependency>
            <groupId>org.gitorious.jcryptionspring</groupId>
            <artifactId>jcryption-spring</artifactId>
            <version>0.1.2</version>
</dependency>
```

now we should add the javascript libraries; dowload them from jCryption zip package on Google code(http://code.google.com/p/jcryption/downloads/list) or source code on github (https://github.com/HazAT/jCryption)

Change your layout file  src/main/webapp/WEB-INF/layouts/default.jspx in order to include js libraries in every page 

```XML
<spring:url var="home" value="/" />
        
<script src="${home }js/jquery-1.4.2.min.js" type="text/javascript"><!--  --></script>  
<script src="${home }js/jquery-ui-1.8.2.custom.min.js" type="text/javascript"><!--  --></script>  
<script type="text/javascript"    src="${home }js/security/jquery.jcryption-1.2.js"><!--  --></script>
```

Then hook the cryptong function to each form you want to alter (e.g. src/main/webapp/WEB-INF/views/userprofiles/create.jspx and/or src/main/webapp/WEB-INF/views/userprofiles/update.jspx) adding (alter it to accommodate the correct form id):

```javascript
<spring:url value="/EncryptionServlet?generateKeypair=true"  var="getKeysURL"/>
<script>  
/*<![CDATA[ */  

$(function() {

$("#userProfile").jCryption({getKeysURL:"${getKeysURL}"});

});
/*]]>*/
</script> 
```

To catch the /EncryptionServlet call you can add the following lines in the mvc configuration src/main/webapp/WEB-INF/spring/webmvc-config.xml:

```XML
<context:component-scan base-package="org.gitorious.jcryptionspring">
</context:component-scan>
```

and now we must configure the servlet decrypting in src/main/webapp/WEB-INF/web.xml

```XML
<filter>
    <filter-name>DecryptParameters</filter-name>  
    <filter-class>org.gitorious.jcryptionspring.ParameterDecryptingFilter</filter-class>  
</filter>  
<filter-mapping>  
    <filter-name>DecryptParameters</filter-name>  
    <url-pattern>/*</url-pattern>  
</filter-mapping>
```

this should be added before the CharacterEncodingFilter in order to maintain the filters order correct (the Spring OpenEntityManagerInViewFilter must stay in place to avoid  "Detached entry passed to persist" error)

Easy isn't it? ;-)


