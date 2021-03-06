/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucenetew;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class LuceneTEW {

    static StringBuilder leerArchivos(File[] archivos) throws FileNotFoundException, IOException{
        try{
        BufferedReader br;
        StringBuilder sb;
        sb = new StringBuilder();
        for (File f : archivos) {
            if (f.isFile() && f.canRead()) {
                //System.out.println("El archivo encontrado es: " + f.getName());
                br = new BufferedReader(new FileReader(f));
                String tempLine =  br.readLine();
                String nombreTemp = f.getName();
                String tipo = nombreTemp.split("\\+")[1];
                String anio = (nombreTemp.split("\\+")[0]).split("_")[0];
                String marca = (nombreTemp.split("\\+")[0]).split("_")[1];
                String modelo = (nombreTemp.split("\\+")[0]).split("_")[2];
                
                while(tempLine!=null){
                    if (tempLine.contains("<DOCNO>")) {
                        
                        //tempLine = "<DOCNO>"+nombreTemp.split("\\+")[0]+"</DOCNO>";
                        //tempLine = "<DOCNO>"+nombreTemp+"----</DOCNO>";
                        //sb.append(tempLine);
                        tempLine = br.readLine();
                        
                        //tempLine = "<MODEL>"+nombreTemp.split("\\+")[1]+"</MODEL>";
                        //sb.append(tempLine);
                    }
                    else if(tempLine.contains("<DOC>"))
                    {
                        tempLine = tempLine.replace("<DOC>", "\n<DOC>\n<YEAR>"+anio+"</YEAR>\n<BRAND>"+marca+"</BRAND>\n<MODEL>"+modelo+"</MODEL>\n<BODY>"+tipo+"</BODY>");
                        sb.append(tempLine);
                        tempLine = br.readLine();
                    }
                    else{
                        sb.append(tempLine);
                        sb.append(System.lineSeparator());
                        tempLine = br.readLine();
                    }
                    //System.out.println(f.getName()+"---"+tempLine);
                }
            }
        }
        return sb;
        }
        catch(FileNotFoundException fne){
            return new StringBuilder();
        } 
        catch(IOException ioe){
            return new StringBuilder();
        }
    }
    
    public static void ParseXML(StringBuilder sb) throws IOException, ParseException {
        
        String indexPath = "C:\\Users\\Hp Kevin\\Documents\\NetBeansProjects\\LuceneTEW\\Indice";
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);
        org.apache.lucene.document.Document doc;
        
        //... Continuar aqui!
        
        String xml = sb.toString();  
        xml = limpiarXML(xml);              
        //System.out.println(xml);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
    try  
    {  
        builder = factory.newDocumentBuilder();  
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        NodeList nSubList;
        Node tempNodo, tempSubNodo;
        NodeList nList = document.getElementsByTagName("DOC");
        for (int i = 0; i < nList.getLength(); i++) {
            tempNodo = nList.item(i);
            nSubList = tempNodo.getChildNodes();
            doc = new org.apache.lucene.document.Document();
            for (int j = 0; j < nSubList.getLength(); j++) {
                tempSubNodo = nSubList.item(j);
                if(!tempSubNodo.getNodeName().contains("#")){
                    //System.out.println(tempSubNodo.getNodeName());
                    //System.out.println(tempSubNodo.getTextContent());
                    //LLenar indice
                    
                    doc.add(new StringField(tempSubNodo.getNodeName(), tempSubNodo.getTextContent(), Field.Store.YES));   
                    System.out.println(tempSubNodo.getNodeName()+ ' '+ tempSubNodo.getTextContent());
                }
            }
            writer.addDocument(doc);
           // System.out.println(doc.toString());
        }
        //System.out.println(document);
        //System.out.println(nList.getLength());
        writer.close();
        System.out.println("Indice Creado");
        
    } catch (ParserConfigurationException | SAXException | IOException | DOMException e) {  
    }
    
    
    //Reader
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
    IndexSearcher searcher = new IndexSearcher(reader);
    Analyzer analyzer2 = new StandardAnalyzer();
    QueryParser parser = new QueryParser("BRAND", analyzer2);
    org.apache.lucene.search.Query query = parser.parse("jeep");
    TopDocs results = searcher.search(query,100000);
    ScoreDoc[] hits = results.scoreDocs;
    
    
    System.out.println(hits.length);

    }
    public static String limpiarXML(String xml){
        
        xml = xml.replace("around</", "around </");
        xml = xml.replace("around<", "around less than");
        xml = xml.replace("&", "and");
        xml = xml.replace("<10", "less than 10");
        xml = xml.replace("< 4", "less than 4");
        xml = xml.replace("<$", "less than $");
        xml = xml.replace("C</", "C </");
        xml = xml.replace("C<", "C less than");
        xml = xml.replace("<70", "less than 70");
        xml = xml.replace("<2000", "less than 2000");
        xml = xml.replace("> or <1500", "or 1500");
        xml = xml.replace("<1K and broken-in >1.5K", "broken");
        xml = xml.replace("<1", " less than 1");
        xml = xml.replace("<2", "less than 2");
        xml = xml.replace("<azda's", "Mazda's");
        xml = xml.replace("< 3", "less than 3");
        xml = xml.replace("< 6", "less than 6");
        xml = xml.replace("< 2", "less than 2");
        xml = xml.replace("$30K<", "$30K less than");
        xml = xml.replace("< 1", "less than 1");
        xml = xml.replace("<5", "less than 5");
        xml = xml.replace("\">", "");
        xml = xml.replace("<I\'", "I");
        xml = xml.replace("<6", "less than 6");
        xml = xml.replace("car</", "car </");
        xml = xml.replace("car<", "car less than");
        xml = xml.replace("< up ", "less than up");
        xml = xml.replace("<3", "less than 3");
        xml = xml.replace("<waaah>", "waaah");
        xml = xml.replace("<great>", "great");
        xml = xml.replace("<--", "--");
        xml = xml.replace("<cracked>", "cracked");
        xml = xml.replace("<hoot>", "hoot");
        xml = xml.replace("BUT</", "BUT </");
        xml = xml.replace("BUT<", "BUT"); 
        
        
        return xml;
    }
    
    public static void main(String[] args) throws IOException, ParseException {
        File dir1 = new File("C:\\Users\\Hp Kevin\\Documents\\NetBeansProjects\\LuceneTEW\\Origen\\2007");
        File dir2 = new File("C:\\Users\\Hp Kevin\\Documents\\NetBeansProjects\\LuceneTEW\\Origen\\2008");
        File dir3 = new File("C:\\Users\\Hp Kevin\\Documents\\NetBeansProjects\\LuceneTEW\\Origen\\2009");
        File[] archivos = dir1.listFiles();
        
        StringBuilder archivo1, archivo2, archivo3;
        archivo1 = leerArchivos(archivos);
        archivos = dir2.listFiles();
        archivo2 = leerArchivos(archivos);
        archivos = dir3.listFiles();
        archivo3 = leerArchivos(archivos);
        
        archivo1.append(archivo2);
        archivo1.append(archivo3);
        archivo1.append("</ROOT>");
        
        StringBuilder archFinal = new StringBuilder();
        archFinal.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        archFinal.append("<ROOT>");
        archFinal.append(archivo1);
        //System.out.println(archFinal);
        ParseXML(archFinal);
        
    }
   

}

