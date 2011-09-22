jCryption for Spring
====================

Use this module to implement a client-server JavaScript to Java based encryption system without the need <sic> of SSL.
This library requires the use of jCryption (http://www.jcryption.org) a JavaScript/PHP framework based on jQuery.

The objectievs for this springframework integration module are:

* get rid of the PHP component
* do not inject (decuple) decrypting code into the standard mvc controllers

The framework implements the following process:

    il client al momento della POST chiama un controller che rende la chiave pubblica di codifica
    la libreria javascript crypta tutti i campi della form in una unica stringa di testo che viene POSTata al controller
    un filtro intercetta le chiamate che contengono nel payload la variabile cryptata la decripta  e wrappa la richiesta http in modo da servire le medesime variabili della POST originale
    a questo punto la catena dei filtri prosegue come al solito

 In questo modo è per esempio possibile realizzare una applicazione standard inserendo in pochi punti quanto serve la il crypting; proviamo a farlo passo passo con l'ausilio di roo:

esegui i seguenti comandi nella shell di roo ini modo da inizializzare una web application che consenta di creare e modificare un oggetto di tipo UserProfile con alcune proprietà

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
dependency add --artifactId jcryption-spring --groupId org.gitorious.jcryptionspring --version 0.1.1 
quit
```


lo script precedente aggiunge al pom.xml la dipendenza dal mio modulo di integrazione

```XML
<dependency>
            <groupId>org.gitorious.jcryptionspring</groupId>
            <artifactId>jcryption-spring</artifactId>
            <version>0.1.2</version>
</dependency>
```


a questo punto è necessario aggiungere le librerie javascript che possono essere scaricate dal sito di jCryption.

Modifica il file src/main/webapp/WEB-INF/layoutsdefault.jspx in modo che le librerie js vengano incluse in ogni pagina

```XML
<spring:url var="home" value="/" />
        
<script src="${home }js/jquery-1.4.2.min.js" type="text/javascript"><!--  --></script>  
<script src="${home }js/jquery-ui-1.8.2.custom.min.js" type="text/javascript"><!--  --></script>  
<script type="text/javascript"    src="${home }js/security/jquery.jcryption-1.2.js"><!--  --></script>
```


A questo punto bisogna agganciare la funziona di crypting alla post di ciascuna form (p.e. src/main/webapp/WEB-INF/views/userprofiles/create.jspx e src/main/webapp/WEB-INF/views/userprofiles/update.jspx) inserendo :

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

per fare in modo che la chiamata  /EncryptionServlet abbi luogo puoi aggiungere alla configurazione mvc src/main/webapp/WEB-INF/spring/webmvc-config.xml:

```XML
<context:component-scan base-package="org.gitorious.jcryptionspring">
</context:component-scan>
```


infine è necessario configurare il filtro di decripting nel src/main/webapp/WEB-INF/web.xml inserendo

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


prima del CharacterEncodingFilter in maniera che l'ordine dei filtri resti inalterato (lo Spring OpenEntityManagerInViewFilter deve rimanere al suo posto per non incorrere nel fatidico "Detached entry passed to persist"

Semplice no?


