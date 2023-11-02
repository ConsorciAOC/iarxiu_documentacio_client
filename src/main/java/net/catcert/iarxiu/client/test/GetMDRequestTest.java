package net.catcert.iarxiu.client.test;

import java.io.File;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.dissemination.GetMDRequestDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetMDResponseDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetMDRequestDocument.GetMDRequest;


/**
 * Classe d'exemple de petició de recuperació d'un determinat vocabulari de 
 * metadades que forma part d'un paquet a iArxiu.
 */
public class GetMDRequestTest {

	public static void main(String[] args) throws Exception {
		try {
			String fileSeparator = File.separator;
			String userDir = System.getProperty("user.dir").replace("/", fileSeparator);
			String pathOutDir = userDir + "/out/samples/".replace("/", fileSeparator);
			Utils.createIfNotExistsDirectory(pathOutDir);

			// Càrrega de l'application context
			ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
	
			// Client
			ProxyClient proxy = (ProxyClient) context.getBean("proxyClient");
			
			// Petició de cerca de metadades
			GetMDRequestDocument requestDocument = GetMDRequestDocument.Factory.newInstance();
			GetMDRequest request = requestDocument.addNewGetMDRequest();
			
			// Identificador del paquet
			request.setPackageId("catcert:m02396:20230117-134638756:8169");
			
			// Identificador de les metadades
			request.setMetadataId("DMD_1");
			
			// Pintem la petició
			Utils.printXmlObject(requestDocument);
			
			// Guardem la petició en un fitxer
			requestDocument.save(new File(pathOutDir + "GetMDRequest.xml"));
			
			// Enviament de la petició
			GetMDResponseDocument responseDocument = (GetMDResponseDocument)proxy.send(requestDocument);
			
			// Pintem la resposta
			Utils.printXmlObject(responseDocument);
			
			// Guardem la resposta en un fitxer
			responseDocument.save(new File(pathOutDir + "GetMDResponse.xml"));
			
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		
	}
	
}
