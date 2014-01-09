package com.setfive

import collection.immutable.SortedMap
import scalaj.http.Http
import scalaj.http.HttpOptions
import org.htmlcleaner.HtmlCleaner
import java.io._

class AVC {
  
  def extractPostDataFromFiles() = {
    
    val dir = new File("/home/ashish/Downloads/downloaded_urls/")
    val extractedData = dir.listFiles()
       					   .filter( (f) => f.getName().indexOf(".html") > -1 )
       					   .foldLeft( List[BlogPost]() )( (a, el) => a ::: getPostDataFromFile(el))
    
    /*
    val groupedMonthData = extractedData.groupBy(f => {           
      f.datetime.split("-").take(2).mkString("-")      
    }).map({
      case (k, v) => (k -> combineWordCountsForMonth(v))
    })
    writeGroupedDataCSV("avc_by_month.csv", groupedMonthData)
    */
    
    val groupedYearData = extractedData.groupBy(f => {        
      Integer.valueOf(f.datetime.split("-").take(1).mkString("-"))      
    }).map({
      case (k, v) => (k -> combineWordCountsForMonth(v))
    })    
        
    writeGroupedDataCSV("avc_by_year.csv", groupedYearData)
  }
  
  def writeGroupedDataCSV(targetFile: String, groupedData: Map[Integer, List[(String, Int)]]) = {
    val csvFile = new PrintWriter(new File("/home/ashish/Downloads/downloaded_urls/" + targetFile))
    
    val csvData = 
      groupedData.foldLeft( new StringBuilder("date,word,count,used_last_year\n") )((a, el) => {    	  
	      val (date, wordCounts) = el
	      val lastYearList = groupedData.getOrElse(date - 1, List[(String, Int)]())
	      var ax = a
	      
	      for( (w, c) <- wordCounts )
	        ax = ax.append(date + "," + w 
	        					+ "," + c
	        					+ "," + ( if (lastYearList.exists({ case (lw, lc) => lw == w})) "1" else "0" )
	        					+ "\n")
	      
	      ax
    })
    
	csvFile.write(csvData.mkString)
	csvFile.close    
  }
  
  def combineWordCountsForMonth(postList: List[BlogPost]):List[(String, Int)] = {        
    val mapList = postList.map((el) => el.wordCount)       
    var combinedMap = Map.empty[String, Int]
    
    for( mp <- mapList; (k, v) <- mp){
      val newVal = combinedMap.getOrElse(k, 0) + v
      combinedMap = (combinedMap - k) ++ Map( k -> newVal )      
    }
            
    return combinedMap.toList.sortBy({case(k, v) => -v}).take(100)
  }
  
  def getWordCountForPost(body: String): Map[String, Int] = {
    
    val nonWord = "[^\\w]+|nbsp".r
    val splitWords = body.split(" ")
    					 .map(el => nonWord.replaceAllIn(el.toLowerCase(), ""))
    					 .filter(el => el.length() > 5)    					 
    
    return splitWords.toList.foldLeft( Map[String, Int]() )((a, el) => {      
      val newVal = a.getOrElse(el, 0) + 1            
      (a - el) ++ Map( el -> newVal )
    })
  }
  
  def getPostDataFromFile(file: File): List[ BlogPost ] = {       
    val rootNode = (new HtmlCleaner).clean( readFile(file.getName()) )
    
    val extractedPosts = rootNode.getElementsByAttValue("class", "entry", true, false)
    		.foldLeft( List[BlogPost]() )( (a, el) => {
    		  
    		  val dateTime = el.findElementByName("time", true)    		  
    		  val title = el.findElementByAttValue("class", "entry-header", true, false)
    		  val postBody = el.findElementByAttValue("class", "entry-content", true, false)
    		  
    		  if( dateTime == null || postBody == null || title == null )
    		    return a
    		      		      		      		  
    		  val extractedPost = new BlogPost(
    		      dateTime.getAttributeByName("datetime"),
    		      title.getText().toString().trim(),
    		      postBody.getText().toString().trim(), 
    		      getWordCountForPost( postBody.getText().toString().trim() )
    		  )
    		      		  
    		  a :+ extractedPost
    })
    
    return extractedPosts
  }
  
  def downloadArchiveUrls() = {
    
    val urlList = if( readFile("urlList.txt").length() > 0 ){
      readFile("urlList.txt").split("[\r\n]+").toSet
    }else{
      getMonthPostUrls
    }    
    
    writeFile("urlList.txt", urlList.mkString("\n"))
    
    urlList.foreach((url) => {
      
      val filename = if( url.indexOf("index.html") > -1 ){
    	  url.split("/").slice(4, 6).mkString("") + "page1.html"
      }else{
    	  url.split("/").slice(4, 8).mkString("") + ".html"
      }
      
      val request = Http( url )
    				.header("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0")
    				.option(HttpOptions.readTimeout(20000))
    				.option(HttpOptions.connTimeout(1000))
            
      writeFile(filename, request.asString)      
      
      println( url )
      Thread.sleep(1000)      
    })
    
  }
  
  def getMonthPostUrls(): Set[String] = {       
    val request = Http("http://www.avc.com/a_vc/archives.html")
    				.header("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0")
    				.option(HttpOptions.readTimeout(20000))
    				.option(HttpOptions.connTimeout(1000))
    
    val rootNode = (new HtmlCleaner).clean( request.asString )      
    
    val archiveDiv = rootNode.getElementsByAttValue("class", "archive-date-based archive", true, true)(0)    
    var monthLinks = archiveDiv
    					.getElementsByAttValue("class", "archive-list-item", true, true)
    					.foldLeft( Set[String]() )( (a, el) => a + el.getElementsByName("a", true)(0).getAttributeByName("href") )
    
    var pgLinks = monthLinks
    while( pgLinks.size > 0 ){      
      pgLinks = pgLinks.map(a => getPagerLink(a)).filter((a) => a.length() > 0)
      monthLinks = monthLinks.++(pgLinks)      
    }    
    
    return monthLinks
  }
  
  def getPagerLink(archiveUrl: String): String = {
    val request = Http(archiveUrl)
    				.option(HttpOptions.readTimeout(20000))
    				.option(HttpOptions.connTimeout(1000))
    				.header("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0")    				
        
    val rootNode = (new HtmlCleaner).clean( request.asString )      
    val pagerRights = rootNode.getElementsByAttValue("class", "pager-right", true, false)    
    
    println(archiveUrl)
    
    return if (pagerRights.length > 0) pagerRights(0).findElementByName("a", true).getAttributeByName("href") else "";    
    
  }
  
  def readFile(filename: String): String = {
    try {
    	return scala.io.Source.fromFile("/home/ashish/Downloads/downloaded_urls/" + filename).mkString
    }catch{
      case _: Throwable => return ""
    }   
  }
  
  def writeFile(filename: String, data: String) = {
    val pw = new PrintWriter( new File("/home/ashish/Downloads/downloaded_urls/" + filename) )
	pw.write(data)
	pw.close
  }
  
}