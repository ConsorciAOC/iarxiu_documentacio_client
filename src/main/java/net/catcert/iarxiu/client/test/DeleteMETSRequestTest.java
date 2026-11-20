package net.catcert.iarxiu.client.test;

import java.io.File;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.administration.DeleteMETSRequestDocument;
import com.hp.iarxiu.core.schemas.x20.administration.DeleteMETSResponseDocument;
import com.hp.iarxiu.core.schemas.x20.administration.DeleteMETSRequestDocument.DeleteMETSRequest;

/**
 * Classe d'exemple de petició d'eliminació d'un paquet METS d'iArxiu.
 */
public class DeleteMETSRequestTest {

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

			// Petició d'eliminació de paquet METS
			DeleteMETSRequestDocument requestDocument = DeleteMETSRequestDocument.Factory.newInstance();
			DeleteMETSRequest request = requestDocument.addNewDeleteMETSRequest();

			// Identificador del paquet METS
			request.setMetsId("catcert:m02396:YYYYMMDD-134638756:8169");

			// Pintem la petició
			Utils.printXmlObject(requestDocument);

			// Guardem la petició en un fitxer
			requestDocument.save(new File(pathOutDir + "DeleteMETSRequest.xml"));

			// Enviament de la petició
			DeleteMETSResponseDocument responseDocument = (DeleteMETSResponseDocument) proxy.send(requestDocument);

			// Pintem la resposta
			Utils.printXmlObject(responseDocument);

			// Guardem la resposta en un fitxer
			responseDocument.save(new File(pathOutDir + "DeleteMETSResponse.xml"));

		} catch (Exception e) {

			System.out.println(e.getMessage());

		}

	}

}
