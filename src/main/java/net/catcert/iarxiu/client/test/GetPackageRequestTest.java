package net.catcert.iarxiu.client.test;

import java.io.File;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.dissemination.GetPackageRequestDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetPackageResponseDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetPackageRequestDocument.GetPackageRequest;


/**
 * Classe d'exemple de petició de recuperació d'un paquet a iArxiu. El format en què es recupera és METS.
 */
public class GetPackageRequestTest {

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
			
			// Petició de recuperació de paquet
			GetPackageRequestDocument requestDocument = GetPackageRequestDocument.Factory.newInstance();
			GetPackageRequest request = requestDocument.addNewGetPackageRequest();
			
			// Iidentificador del paquet
			request.setPackageId("catcert:m02396:20230117-134638756:8169");
			
			// Incloure metadades d'acord a l'esquema Dublin Core
			request.setIncludeDC(true);
			Utils.printXmlObject(requestDocument);
			requestDocument.save(new File(pathOutDir + "GetPackageRequest_withoutBinaries.xml"));
			
			// Enviament de la petició
			GetPackageResponseDocument responseDocument = (GetPackageResponseDocument)proxy.send(requestDocument);

			Utils.printXmlObject(responseDocument);
			
			// Guardem la resposta en un fitxer
			responseDocument.save(new File(pathOutDir + "GetPackageResponse_withoutBinaries.xml"));
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
