package net.catcert.iarxiu.client.test;

import java.io.File;

import net.catcert.iarxiu.client.proxy.ProxyClient;
import net.catcert.iarxiu.client.test.utils.Utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hp.iarxiu.core.schemas.x20.administration.CheckUserRequestDocument;
import com.hp.iarxiu.core.schemas.x20.administration.CheckUserResponseDocument;
import com.hp.iarxiu.core.schemas.x20.administration.CheckUserRequestDocument.CheckUserRequest;

/**
 * Classe d'exemple de petició de validació d'usuari per a una operació
 * d'administració d'iArxiu.
 */
public class CheckUserRequestTest {

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

			// Petició de validació d'usuari
			CheckUserRequestDocument requestDocument = CheckUserRequestDocument.Factory.newInstance();
			CheckUserRequest request = requestDocument.addNewCheckUserRequest();

			// Dades de validació
			request.setNif("12345678Z");
			request.setOrganization("CATCERT");
			request.setFonds("FONS_PROVES");
			request.setPackageId("catcert:m02396:20230117-134638756:8169");

			// Pintem la petició
			Utils.printXmlObject(requestDocument);

			// Guardem la petició en un fitxer
			requestDocument.save(new File(pathOutDir + "CheckUserRequest.xml"));

			// Enviament de la petició
			CheckUserResponseDocument responseDocument = (CheckUserResponseDocument) proxy.send(requestDocument);

			// Pintem la resposta
			Utils.printXmlObject(responseDocument);

			// Guardem la resposta en un fitxer
			responseDocument.save(new File(pathOutDir + "CheckUserResponse.xml"));

		} catch (Exception e) {

			System.out.println(e.getMessage());

		}

	}

}
