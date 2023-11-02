package net.catcert.iarxiu.client.test;

import java.io.File;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.dissemination.FindIDsRequestDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.FindIDsRequestDocument.FindIDsRequest;
import com.hp.iarxiu.core.schemas.x20.dissemination.FindIDsResponseDocument;
import com.hp.iarxiu.core.schemas.x20.indexer.SearchType;


/**
 * Classe d'exemple de cerca de paquets a iArxiu que compleixen certs criteris 
 * de metadades. Els criteris de cerca surten d'un fitxer xml. 
 * 
 * Nota: és possible que la resposta sigui buida si no es troba cap paquet 
 * que compleixi els criteris de cerca.
 */
public class FindIDsRequestTest {

	public static void main(String[] args) {
		try {
			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replace("/", fileSeparator);
			String pathOutDir = userDir + "/out/samples/".replace("/", fileSeparator);
			Utils.createIfNotExistsDirectory(pathOutDir);

			ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

			ProxyClient proxy = (ProxyClient) context.getBean("proxyClient");

			FindIDsRequestDocument requestDocument = FindIDsRequestDocument.Factory.newInstance();
			FindIDsRequest request = requestDocument.addNewFindIDsRequest();
			
			// Fitxer XML que conté els criteris de cerca
			File searchFile = new File(userDir +
					"/src/main/resources/net/catcert/iarxiu/client/test/basicSearchSample.xml".replace("/", fileSeparator));

			SearchType search = request.addNewSearch();
			search.set(SearchType.Factory.parse(searchFile));

			Utils.printXmlObject(requestDocument);
			requestDocument.save(new File(pathOutDir + "FindIDsRequest.xml"));

			FindIDsResponseDocument responseDocument = (FindIDsResponseDocument)proxy.send(requestDocument);
			Utils.printXmlObject(responseDocument);
			responseDocument.save(new File(pathOutDir + "FindIdsResponse.xml"));
			
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}
