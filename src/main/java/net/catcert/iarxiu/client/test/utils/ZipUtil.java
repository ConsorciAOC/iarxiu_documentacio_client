package net.catcert.iarxiu.client.test.utils;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil
{
    List<String> fileList;
    private String OUTPUT_ZIP_FILE;
    private String SOURCE_FOLDER;
	
    public ZipUtil(String OUTPUT_ZIP_FILE, String SOURCE_FOLDER){
    	this.OUTPUT_ZIP_FILE = OUTPUT_ZIP_FILE;  
    	this.SOURCE_FOLDER = SOURCE_FOLDER;
    	fileList = new ArrayList<String>();
    	generateFileList(new File(SOURCE_FOLDER));    	
    }
	      
   
    public void zipIt(){
	     byte[] buffer = new byte[1024];
	    	
	     try{	    		
	    	FileOutputStream fos = new FileOutputStream(OUTPUT_ZIP_FILE);
	    	ZipOutputStream zos = new ZipOutputStream(fos);
	    	zos.setLevel(9);
	    	System.out.println("Output to Zip : " + OUTPUT_ZIP_FILE);
	    		
	    	for(String file : this.fileList){    			
	    		System.out.println("File Added : " + file);
	    		ZipEntry ze= new ZipEntry(file);
	        	zos.putNextEntry(ze);               
	        	FileInputStream in = 
	                       new FileInputStream(SOURCE_FOLDER + File.separator + file);       	   
	        	int len;
	        	while ((len = in.read(buffer)) > 0) {
	        		zos.write(buffer, 0, len);
	        	}               
	        	in.close();
	    	}    		
	    	zos.closeEntry();
	    	zos.close();          
	    	System.out.println("Done");
	    }
	     catch(IOException ex){
	       ex.printStackTrace();   
	    }
   }    
 
    private void generateFileList(File node){
    	//add file only
		if(node.isFile()){
			fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
		}			
		if(node.isDirectory()){
			String[] subNote = node.list();
			for(String filename : subNote){
				generateFileList(new File(node, filename));
			}
		} 
    }
 
    private String generateZipEntry(String file){
    	return file.substring(SOURCE_FOLDER.length()+1, file.length());
    }
}