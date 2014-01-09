package com.setfive

import scalaj.http.Http
import scalaj.http.HttpOptions
import org.htmlcleaner.HtmlCleaner
import java.io._

object Main {

  def main(args: Array[String]): Unit = {
    /*
    val request = Http("http://www.avc.com/a_vc/2013/12/index.html")
    				.option(HttpOptions.readTimeout(20000))
    				.option(HttpOptions.connTimeout(1000))
    				.header("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0")    				
        	    	
    val cleaner = new HtmlCleaner
    val rootNode = cleaner.clean( request.asString )      
    val pagerRights = rootNode.getElementsByAttValue("class", "pager-right", true, false)    
    
    val pagerUrl = if (pagerRights.length > 0) pagerRights(0).findElementByName("a", true).getAttributeByName("href") else "";    
    */
    
    // for( el <- archiveDiv.getElementsByAttValue("class", "archive-list-item", true, true) )
    //  println( el.getElementsByName("a", true)(0).getAttributeByName("href") )
    
    
    // for( el <- rootNode.getElementsByAttValue("class", "pager-right", true, false) )
      
    
    // for( el <- rootNode.getElementsByAttValue("class", "pager-right", true, false) )
    //  println( el.getText() )
    
    // for( el <- rootNode.getElementsByAttValue("class", "entry", true, true) )
    
    /*
	val pw = new PrintWriter( new File("/home/ashish/Downloads/downloaded_urls/test.txt") )
	pw.write("Hello, world")
	pw.close    
    */
    
    val avc = new AVC
    avc.extractPostDataFromFiles()
  }

}