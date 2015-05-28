package edu.sjsu.cs.cs218;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.appengine.api.datastore.DatastoreService; 
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/** A simple REST server
 * @author      YU LI <yuqianli188@hotmail.com>
 * @version     1.0
 * @since       2015-04-19    
 */

@SuppressWarnings("serial")
public class CS218_HW2Servlet extends HttpServlet 
{	
	//get datastore object from DatastoreServiceFactory
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    //GET method
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
    	
    	resp.setContentType("text/xml");
        String str = req.getPathInfo(); 
        String[] tokens = str.split("/");
        String id = null;
        String lastName = null;
        String firstName = null; 
        String name = null;
        String budget = null;
        Key keyCheck = null;
        int tokenLength = tokens.length;
        
        System.out.println(tokenLength); 
        
        // if token length = 2, it's for GET /rest/employee
        if (tokenLength==2)
        {
        	 if(tokens[1].equals("employee"))
        	 {
	        	Query q = new Query("employee"); 
	            PreparedQuery pq = datastore.prepare(q);  
	            
	            	//Check if the iterator hasNext to see if employee Entity is in the datastore
		           if (pq.asIterable().iterator().hasNext() == false)
		           {
		        	   resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			        	return; 
		           }
		           
		          PrintWriter writer = resp.getWriter();
	 	          writer.write("<employeeList>\n");
	 	            
		        for (Entity employee : pq.asIterable()) 
		        {   
		        	id = employee.getProperty("id").toString(); 
		            firstName = employee.getProperty("firstName").toString();   
		            lastName = employee.getProperty("lastName").toString();   
		            writer.write("\t<employee>\n");
		            writer.write("\t<id>" + id + "</id>\n");  
				    writer.write("\t<firstName>" + lastName + "</firstName>\n");
				    writer.write("\t<lastName>" + firstName+ "</lastName>\n");
				    writer.write("\t</employee>\n");
		        }    	
		        writer.write("</employeeList>\n");              
        	 }   
        	 
        	 if(tokens[1].equals("project"))
        	 {  
	        	Query q = new Query("project"); 
	            PreparedQuery pq = datastore.prepare(q); 
	              
	           //Check if the iterator hasNext to see if project Entity is in the datastore
	           if (pq.asIterable().iterator().hasNext() == false)
	           {
	        	   resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		        	return; 
	           }
	            
	        	PrintWriter writer = resp.getWriter();
	            writer.write("<projectList>\n");
		        for (Entity project : pq.asIterable()) 
		        {   
		        	id = project.getProperty("id").toString(); 
		            name = project.getProperty("name").toString();   
		            budget = project.getProperty("budget").toString();   
		            writer.write("\t<project>\n");
		            writer.write("\t<id>" + id + "</id>\n");  
				    writer.write("\t<name>" + name + "</name>\n");
				    writer.write("\t<budget>" + budget+ "</budget>\n");
				    writer.write("\t</project>\n");
		        }    	
		        writer.write("</projectList>\n");              
        	 }   
     }
        
		// if token length = 3, it's for GET /rest/employee/m 
		if (tokenLength==3)
		{	
			keyCheck = KeyFactory.createKey(tokens[1], tokens[2]);
			if(hasKey(keyCheck)== false)
	        {
	        	resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        	return; 	
	        }
	        if(tokens[1].equals("employee"))
	        {
		        try {
					Entity employee = datastore.get(keyCheck);
					id = employee.getProperty("id").toString(); 
					 lastName = employee.getProperty("lastName").toString(); 
					 firstName = employee.getProperty("firstName").toString(); 
				} catch (EntityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
        
	        if(tokens[1].equals("project"))
	        {
		        try {
					Entity project = datastore.get(keyCheck);
					id = project.getProperty("id").toString(); 
					name = project.getProperty("name").toString(); 
					budget = project.getProperty("budget").toString(); 
				} catch (EntityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	       
	        PrintWriter writer = resp.getWriter();
	        writer.write("<" + tokens[1] + ">\n");
	        writer.write("\t<id>" + id + "</id>\n");
	        if (tokens[1].equals("employee"))
	        {
		        writer.write("\t<firstName>" + lastName + "</firstName>\n");
		        writer.write("\t<lastName>" + firstName+ "</lastName>\n");
	        }
	        
	        if (tokens[1].equals("project"))
	        {
		        writer.write("\t<name>" + name+ "</name>\n");
		        writer.write("\t<budget>" + budget + "</budget>\n");
	        }
	        writer.write("</" + tokens[1] + ">\n");
			}
    }
    
    //POST method
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
    	String str = req.getPathInfo(); 
        String[] tokens = str.split("/");
        String firstToken = tokens[1];
        
        if (firstToken.equals("employee"))
        {
        try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document doc = builder.parse(req.getInputStream());
	        XPathFactory xPathfactory = XPathFactory.newInstance();
	        XPath xpath = xPathfactory.newXPath();
	        XPathExpression expr1 = xpath.compile("/" + firstToken + "/id/text()");
	        int id =  Integer.parseInt((String) expr1.evaluate(doc, XPathConstants.STRING));
	        
	        XPathExpression expr2 = xpath.compile("/" + firstToken + "[id=" +id +"]/firstName/text()");
	        String firstName = (String) expr2.evaluate(doc, XPathConstants.STRING);
	        
	        XPathExpression expr3 = xpath.compile("/" + firstToken + "[id=" +id +"]/lastName/text()");
	        String lastName = (String) expr3.evaluate(doc, XPathConstants.STRING);
	        
	        String key = Integer.toString(id); 
	        
	        Key keyCheck = KeyFactory.createKey(tokens[1], key);
	        if(hasKey(keyCheck)== true)
	        {
	        	resp.setStatus(HttpServletResponse.SC_CONFLICT);
	        	return; 	
	        }
	        
	        //declare an entity with Kind "employee"
	        Entity employee = new Entity("employee", key);
	   
	        //set entity's properties
	        employee.setProperty("id", id);
	        employee.setProperty("firstName", firstName);
	        employee.setProperty("lastName", lastName);
	        //put this entity to datastore
	        datastore.put(employee);
	        
	        resp.setStatus(HttpServletResponse.SC_CREATED);
        	} catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            e.printStackTrace();
        	}
        }
        
        if (firstToken.equals("project"))
        {
        	 try {
     	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     	        DocumentBuilder builder = factory.newDocumentBuilder();
     	        Document doc = builder.parse(req.getInputStream());
     	        XPathFactory xPathfactory = XPathFactory.newInstance();
     	        XPath xpath = xPathfactory.newXPath();
     	        XPathExpression expr1 = xpath.compile("/" + firstToken + "/id/text()");
     	        int id =  Integer.parseInt((String) expr1.evaluate(doc, XPathConstants.STRING));
     	        
     	        XPathExpression expr2 = xpath.compile("/" + firstToken + "[id=" +id +"]/name/text()");
     	        String name = (String) expr2.evaluate(doc, XPathConstants.STRING);
     	        
     	        XPathExpression expr3 = xpath.compile("/" + firstToken + "[id=" +id +"]/budget/text()");
     	        float budget = Float.parseFloat((String)expr3.evaluate(doc, XPathConstants.STRING));
     	        
     	        String key = Integer.toString(id); 
     	       
     	        Key keyCheck = KeyFactory.createKey(tokens[1], key);
	   	        if(hasKey(keyCheck)== true)
	   	        {
	   	        	resp.setStatus(HttpServletResponse.SC_CONFLICT);
	   	        	return; 	
	   	        }
     	        
     	        //declare an entity with Kind "employee"
     	        Entity project = new Entity("project", key);

     	        //set entity's properties
     	        project.setProperty("id", id);
     	        project.setProperty("name", name);
     	        project.setProperty("budget", budget);
     	        //put this entity to datastore
     	        datastore.put(project);
     	        
     	        resp.setStatus(HttpServletResponse.SC_CREATED);
             }catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            e.printStackTrace();
            }
        }
    }
    
  //PUT method
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
    	
    	String str = req.getPathInfo(); 
        String[] tokens = str.split("/");
        String firstToken = tokens[1];
        
        Key keyCheck = KeyFactory.createKey(tokens[1], tokens[2]);
        
        if(hasKey(keyCheck)== false)
        {
        	resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        	return; 	
        }
        
        if (firstToken.equals("employee"))
        {
        try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	       
	        Document doc = builder.parse(req.getInputStream());
	        XPathFactory xPathfactory = XPathFactory.newInstance();
	        XPath xpath = xPathfactory.newXPath();

	        XPathExpression expr1 = xpath.compile("//*");
	        Object result = expr1.evaluate(doc, XPathConstants.NODESET);
	        NodeList nodes = (NodeList) result;
	          
	        Entity employee = datastore.get(keyCheck);
	        
	        for (int i = 0; i < nodes.getLength(); i++) {
	            Node node = nodes.item( i );
	            String nodeName = node.getNodeName();
	            String nodeValue = node.getChildNodes().item( 0 ).getNodeValue();
	            
	            if( nodeName.equals( "firstName" ) ) {
	                        String firstName = nodeValue;
	                        employee.setProperty("firstName", firstName);
	            } 
	            else if( nodeName.equals( "lastName" ) ) {
	                        String lastName = nodeValue;
	                        employee.setProperty("lastName", lastName);
	            }
	        }
	        datastore.put(employee);
	        
	        resp.setStatus(HttpServletResponse.SC_OK);
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            e.printStackTrace();
        } catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
        
        if (firstToken.equals("project"))
        {
        try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	       
	        Document doc = builder.parse(req.getInputStream());
	        XPathFactory xPathfactory = XPathFactory.newInstance();
	        XPath xpath = xPathfactory.newXPath();

	        XPathExpression expr1 = xpath.compile("//*");
	        Object result = expr1.evaluate(doc, XPathConstants.NODESET);
	        NodeList nodes = (NodeList) result;
	        
	        Entity project = datastore.get(keyCheck);
	        
	        for (int i = 0; i < nodes.getLength(); i++) {
	            Node node = nodes.item( i );
	            String nodeName = node.getNodeName();
	            String nodeValue = node.getChildNodes().item( 0 ).getNodeValue();
	            
	            if( nodeName.equals( "name" ) ) {
	                        String name = nodeValue;
	                        project.setProperty("name", name);
	            } 
	            else if( nodeName.equals( "budget" ) ) {
	                       float budget = Float.parseFloat(nodeValue);
	                        project.setProperty("budget", budget);
	            }
	        }
	        datastore.put(project);
	        
	        resp.setStatus(HttpServletResponse.SC_OK);
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | IOException e) {
            e.printStackTrace();
        } catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
    }
    
  //DELETE method
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
    	String str = req.getPathInfo(); 
        String[] tokens = str.split("/");
        String firstToken = tokens[1];
          
        Key keyCheck = KeyFactory.createKey(tokens[1], tokens[2]);
        
        if(hasKey(keyCheck)== false)
        {
        	resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        	return; 	
        }
        
        if (firstToken.equals("employee"))
        {
        	datastore.delete(keyCheck);
        }   
        
        if (firstToken.equals("project"))
        {
        	datastore.delete(keyCheck);
        }   
    }
    
    // Check the datastore to see if key exists or not
    public boolean hasKey(Key key){
		try {
			Entity checkKey = datastore.get(key);	
			System.out.println("Key found");
		} catch (EntityNotFoundException e1) {
			// TODO Auto-generated catch block
			System.out.println("No Key found");
			return false; 
		}
		return true;
      }
}
