package com.setfive
case class BlogPost(val datetime: String, val title: String, 
					val body: String, val wordCount: Map[String, Int])