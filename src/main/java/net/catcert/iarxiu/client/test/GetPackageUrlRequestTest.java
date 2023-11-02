package net.catcert.iarxiu.client.test;

import java.io.File;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.dissemination.GetPackageUrlRequestDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetPackageUrlResponseDocument;
import com.hp.iarxiu.core.schemas.x20.dissemination.GetPackageUrlRequestDocument.GetPackageUrlRequest;


/**
 * Classe d'exemple d'obtenció d'URL per a consulta detallada d'un paquet
 */
public class GetPackageUrlRequestTest {

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
			GetPackageUrlRequestDocument requestDocument = GetPackageUrlRequestDocument.Factory.newInstance();
			GetPackageUrlRequest request = requestDocument.addNewGetPackageUrlRequest();
			
			// Identificador del paquet
			request.setPackageId("catcert:m02396:20230117-134638756:8169");
			Utils.printXmlObject(requestDocument);
			requestDocument.save(new File(pathOutDir + "GetPackageUrlRequest.xml"));
			
			// Enviament de la petició
			GetPackageUrlResponseDocument responseDocument = (GetPackageUrlResponseDocument)proxy.send(requestDocument);
			
			// Obtenim la url, la mostrem a la consola i la desem
			String urlViewer = responseDocument.getGetPackageUrlResponse().getPackageUrl();
			System.out.println("\n" + "URL: " + urlViewer + "\n");
			Utils.printXmlObject(responseDocument);
			responseDocument.save(new File(pathOutDir + "GetPackageUrlResponse.xml"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
