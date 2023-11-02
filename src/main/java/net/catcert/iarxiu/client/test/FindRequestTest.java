package net.catcert.iarxiu.client.test;

import java.io.File;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.dissemination.FindRequestDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.FindResponseDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.FindRequestDocument.FindRequest;
import com.hp.iarxiu.core.schemas.x20.indexer.SearchType;


/**
 * Classe d'exemple de cerca de paquets a iArxiu que compleixen certs criteris 
 * de metadades. És semblant a FindIDRequest però retorna molta més informació 
 * de cada paquet. El nombre de resultats es pot limitar amb el paràmetre 
 * maxSearchResults de la request. * 
 */
public class FindRequestTest {

	public static void main(String[] args) {
		try {
			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replace("/", fileSeparator);
			String pathOutDir = userDir + "/out/samples/".replace("/", fileSeparator);
			Utils.createIfNotExistsDirectory(pathOutDir);

			// Càrrega de l'application context
			ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
			
			// Client
			ProxyClient proxy = (ProxyClient) context.getBean("proxyClient");
			
			// Petició de cerca de paquet
			FindRequestDocument requestDocument = FindRequestDocument.Factory.newInstance();
			FindRequest request = requestDocument.addNewFindRequest();
			
			// Fitxer XML que conté els criteris de cerca
			File searchFile = new File(userDir +
					"/src/main/resources/net/catcert/iarxiu/client/test/basicSearchSample.xml".replace("/", fileSeparator));

			SearchType search = request.addNewSearch();
			search.set(SearchType.Factory.parse(searchFile));
			request.setMaxSearchResults(10);

			Utils.printXmlObject(requestDocument);
			requestDocument.save(new File(pathOutDir + "FindRequest.xml"));

			FindResponseDocument responseDocument = (FindResponseDocument)proxy.send(requestDocument);
			Utils.printXmlObject(responseDocument);

			responseDocument.save(new File(pathOutDir + "FindResponse.xml"));
			
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}
