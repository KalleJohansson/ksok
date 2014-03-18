package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
    	
    	String urlRelation = 
    			"http://kulturarvsdata.se/ksamsok/api?method=getRelations&relation=isVisualizedBy&objectId=raa/bbr/21000000970020&maxCount=10&x-api=test";
    	String prettyPrint = "?prettyPrint=true";
    	String format = "jsonld";
//    	String format = "rdf";
    	long objectId = 4343L;//21000001123160L;//21000000970020L;
    	String baseUrl = "http://kulturarvsdata.se/raa/bbr/";
    	String url = baseUrl + String.valueOf(objectId);
    	int timeout = 10000;
    	
    	String json = BaseHttp.basicQuery(url, timeout);
    	json = json == null ? "NULL" : json;
        render(json);
    }

}