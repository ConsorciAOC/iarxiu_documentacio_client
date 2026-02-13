# Client Java d'iArxiu
Client per interactuar amb iArxiu. Configurat per fer-se servir a l'entorn de preproducció (amb ens/fons i clau CDA d'autenticació ja existents a iArxiu).

# Requeriments tècnics
- Git
- Java 8
- Maven 3.9.3 (versió probada)

# Compilació i ús
Les següents instruccions estan probades amb un Windows PowerShell 7.3.8. 
Per fer-se servir amb una altra shell caldrà aplicar els canvis necessaris.

1) Descàrrega del codi font
```shell
> git clone git@github.com:ConsorciAOC/iarxiu_documentacio_client.git
```

2) Instal·lar dependències del client al repositori de Maven local:

Ens situem a l'arrel del projecte clonat
```shell
> cd C:\iarxiu_client\
> mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=.\lib\core-schemas-5.0.0.jar -DgroupId=iarxiu.core -DartifactId=core-schemas -Dversion=5.0.0
> mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=.\lib\mets-schema-5.0.0.jar -DgroupId=iarxiu.libs -DartifactId=mets-schema -Dversion=5.0.0 -Dpackaging=jar
> mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=.\lib\saml-schema-assertion-5.0.0.jar -DgroupId=iarxiu.libs -DartifactId=saml-schema-assertion -Dversion=5.0.0 -Dpackaging=jar
> mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=.\lib\opensaml-1.1.jar -DgroupId=opensaml -DartifactId=opensaml -Dversion=1.1 -Dpackaging=jar
```

7. Compilar projecte
```shell
> mvn clean package
...
...
[INFO] --- jar:3.3.0:jar (default-jar) @ iarxiu-client ---
[INFO] Building jar: C:\iarxiu_documentacio_client\target\iarxiu-client-5.0.0.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  7.937 s
[INFO] Finished at: 2023-10-18T11:27:28+02:00
[INFO] ------------------------------------------------------------------------
```
Un cop compilat, les classes de src/main/java/net/catcert/iarxiu/client/test poden ser executat via CLI amb Java o amb un IDE (IntelliJ/Eclipse...).


# Configuració
## SAML
A la classe _net.catcert.iarxiu.client.handler.SamlInterceptor_ es configuren els atributs de les capceleres SAML de cada petició.
Per les proves inicials el client està configurat per poder ingressar a l'ens/fons per defecte.
Per apuntar a un ens/fons concret caldrà configurar els valors següents amb els corresponents al client (veure apartat 3.2.4 de la Guía d'Integradors):

## Trustore
A la classe _net.catcert.iarxiu.client.proxy.ProxyClient_ es configura el trustore per establir la connexió HTTPS amb l'iArxiu. Per defecte el client està configurat amb un trustore amb les CA necessàries per confiar en el certificat d'iArxiu preproducció.
Si es vol apuntar a l'entorn de producció es proporciona ja un trustore configurat per fer-ho.

## applicacionContext.xml
Al _applicationContext.xml_ d'Spring es configuren diversos beans.
Al bean wssInterceptor es configuren les propietats per signar les peticions. Els valors per definits corresponen al certificat d'exemple proporcionat amb el client
Si es vol fer servir un altre certificat caldrà modificar els valors perquè siguin coherents.

```java
NameIDType subjectName = subjectConfirmation.addNewNameID();
subjectName.setStringValue("Username");	// nom d'usuari
...			
addAttribute(attributeStatement, "urn:iarxiu:2.0:names:organizationAlias", "organizationTest");	// nom de l'ens
addAttribute(attributeStatement, "urn:iarxiu:2.0:names:fondsAlias", "fondsTest");				// fons
addAttribute(attributeStatement, "urn:iarxiu:2.0:names:member-of", "archivists");				// grup
```

# Descripció directoris
### iarxiu_client\exemples-METS
Conté exemples de METS per carregar a iArxiu.

### iarxiu_client\out\samples
Conté peticions i respostes d'exemple de les diferents operacions suportades (directori creat després d'executar les classes de test).

### iarxiu_client\wsdl
Conté els fitxers WSDL de les operacions d'ingrés (ingest.wsdl), i consulta (dissemination.wsdl).

### iarxiu_client\lib
Conté les llibreries necessàries pel client d'iArxiu.

# Descripció classes
### net.catcert.iarxiu.client.test.FindIDsRequestTest
Operació de cerca dels identificadors de paquets a iArxiu que acompleixen certs criteris de metadades.

### net.catcert.iarxiu.client.test.FindRequestTest
Operació de cerca de paquets a iArxiu que acompleixen certs criteris de metadades.

### net.catcert.iarxiu.client.test.GetAuthenticCopyRequestTest
Operació de petició de creació de còpia oculta, d'un binari a un paquet a iArxiu.

### net.catcert.iarxiu.client.test.GetBinaryRequestTest
Operació de recuperació d'un determinat binari que forma part d'un paquet a iArxiu.

### net.catcert.iarxiu.client.test.GetCompressedUploadTicketTest
Operació d'exemple de petició per realitzar un ingrés per upload comprimit. En un ingrés
upload comprimit es puja a una URL un zip amb el fitxer mets i els fitxers de l'expedient
dins, com el zip que es fa servir pel OfflineZipIngest.
Un cop s'ha pujat el fitxer zip (només s'admet un) es pot fer la petició d'ingrés i posterior consulta de l'estat d'aquest.

### net.catcert.iarxiu.client.test.GetEvidenceReportRequestTest
Operació de sol·licitud de l'informe d'evidències d'un paquet a iArxiu:

### net.catcert.iarxiu.client.test.GetMDRequestTest
Operació de recuperació d'un determinat vocabulari de metadades que forma part d'un paquet a iArxiu:

### net.catcert.iarxiu.client.test.GetOfflineIngestStatusTest
Operació de consulta de l'estat d'un ingrés fet en mode offline:

### net.catcert.iarxiu.client.test.GetPackageRequestTest
Operació de recuperació d'un paquet a iArxiu. El format en què es recupera és METS:

### net.catcert.iarxiu.client.test.GetPackageUrlRequestTest
Operació d'exemple d'obtenció d'URL per a consulta detallada d'un paquet

### net.catcert.iarxiu.client.test.GetUploadTicketTest
Operació de petició per obtenir un tiquet per realitzar posteriorment un ingrés mitjançant càrrega directa de binaris a iArxiu. Post del/s binari/s.
Els binaris, aniran tots en una carpeta, per exemple Appends, i el codi llegirà tots els binaris per fer el posterior post:

### net.catcert.iarxiu.client.test.GetZipPackageRequestTest
Operació de recuperació d'un paquet a iArxiu, en format zip:

### net.catcert.iarxiu.client.test.MigrateBinaryTest
Operació de migració del format d'un binari prèviament carregat a iArxiu:

### net.catcert.iarxiu.client.test.OfflineIngestTest
Operació de sol·licitud d'inserció d'un PIT a iArxiu, en mode offline:

### net.catcert.iarxiu.client.test.OfflineZipIngestTest
Operació de sol·licitud d'inserció a iArxiu d'un PIT comprimit dins un fitxer ZIP, en mode offline:
