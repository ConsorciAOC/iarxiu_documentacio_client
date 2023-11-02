package net.catcert.iarxiu.client.test.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class Utils {
	

	/**
	 * Prints an XMLObject.
	 * @param xmlObj XMLObject to print
	 * @throws IOException 
	 */
	public static void printXmlObject(XmlObject xmlObj) {	
		
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSaveOuter();
		xmlOptions.setCharacterEncoding("UTF-8");
		
		BufferedOutputStream bos = null;
		InputStream inputStream = null;
		
		try {
			bos = new BufferedOutputStream(System.out);
			int read = 0;
			byte[] bytes = new byte[10000];
			inputStream = xmlObj.newInputStream(xmlOptions);
			while ((read = inputStream.read(bytes)) != -1) {
				bos.write(bytes, 0, read);
				bos.flush();
			}
			bos.write(System.getProperty("line.separator").getBytes());
			bos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/** 
	 * Returns the contents of the file in a byte array.
	 */
    public static byte[] getBytesFromFile(String path) throws IOException {

        File xmlFile = new File(path);
        InputStream is = new FileInputStream(xmlFile);
        byte[] bytes = getBytes(is);
        is.close();

        return bytes;
    }
    
    
    /**
     * Gets a byte array from an InputStream.
     * @param is InputStream
     * @return byte[]
     * @throws IOException
     */
    public static byte[] getBytes(InputStream is) throws IOException {
    
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	
    	int bytee;
    	while (-1!=(bytee=is.read()))
    	{
    	   baos.write(bytee);
    	}
    	baos.close();
    	byte[] bytes = baos.toByteArray();

        is.close();
        return bytes;
    }
	
    
    public static void writeToFile(String path, byte[] data) throws IOException{
    	File fOut = new File(path);
		FileOutputStream fos = new FileOutputStream(fOut);
		fos.write(data);
		fos.close();
    }

    
    public static void writeToFile(String path, InputStream is) throws IOException{
    	System.out.println("Deixant dades a "+path);
    	File fOut = new File(path);
		FileOutputStream fos = new FileOutputStream(fOut);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		try {
			byte[] buffer = new byte[64000];
			int cnt;
			while ((cnt=is.read(buffer)) != -1) {
				bos.write(buffer, 0, cnt);
			}
		} finally {
			is.close();
			bos.close();
		}
    }

	public static InputStream downloadByTicketAsInputStream(String binToken, String downloadUrl) throws Exception {
		System.out.println("Descarregant dades del servidor...");
		PostMethod postMethod = new PostMethod(downloadUrl);
		postMethod.addParameter("tokenId", binToken);
		
		HttpClient httpClient = new HttpClient();
		int status = httpClient.executeMethod(postMethod);
		if (status != HttpStatus.SC_OK) {
        	throw new Exception(HttpStatus.getStatusText(status)+" ["+postMethod.getStatusLine()+"]");
		}
		InputStream response = postMethod.getResponseBodyAsStream();
		return response;
	}

	public static void createIfNotExistsDirectory(String pathOutDir) throws Exception {
		File directory = new File(pathOutDir);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	private static void saveXmlToFile(String filePath, Document doc) throws TransformerException{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(filePath));
		transformer.transform(source, result);
	}

	/* Mètode creat per poder actualitzar una metadada del paquet per poder ingresar-lo sense que el control
	 * d'unicitat el detecti com a repetit i no permeti l'ingrés
	 */
	public static void updateMdDmdFile(String xmlFilePath, String tagnameNodeToModify) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		// Convierte a InputStream el archivo mets
		File xmlFile = new File(xmlFilePath);
		InputStream is = new FileInputStream(xmlFile);

		// parsea a Document el InputStream
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(is);

		// formatea un String con la date actual
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String formattedDate = sdf.format(now);

		// asigna el nuevo valor al nodo pasado por parámetro
		Node dataCreacioNode = doc.getElementsByTagName(tagnameNodeToModify).item(0);
		dataCreacioNode.setTextContent(formattedDate);

		// salva a disco el mets.xml
		saveXmlToFile(xmlFilePath, doc);
	}

	public static void createZipPIT(String zipFilePath, String folderToZip) throws ZipException, IOException{
		ZipUtil zipUtil = new ZipUtil(
				zipFilePath.replace("/", File.separator),
				folderToZip.replace("/", File.separator));
		zipUtil.zipIt();
	}

}