/*
 * Author:  Martin Novak <mn@puakma.net>
 * Project: Puakma Vortex
 * Date:    Dec 20, 2005
 * 
 * Copyright (c) 2004, 2005 webWise Network Consultants Pty Ltd, Australia,
 * http://www.wnc.net.au, all rights reserved
 *
 * Publishing, providing further or using of this program is prohibited
 * without previous written permission of author. Publishing or providing further
 * of the contents of this file is prohibited without prevous written permission
 * of the author.
 */
package puakma.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class contains few helper functions for manipulation with xml.
 *
 * @author Martin Novak
 */
public class XmlUtils
{
  /**
   * This factory creates SAX parser on demand.
   */
  private static final SAXParserFactory saxFactory = SAXParserFactory.newInstance();

  /**
   * This function parses xml via sax parser. The result has to be kept in handler object.
   */
  public static  void parseXml(DefaultHandler handler, String xml)
                               throws SAXException, IOException, ParserConfigurationException
  {
    SAXParser parser;
    parser = saxFactory.newSAXParser();

    StringReader reader = new StringReader(xml);
    InputSource is = new InputSource(reader);
    parser.parse(is, handler);
  }


  /**
   * This function parses xml via sax parser. The result has to be kept in handler object.
   */
  public static  void parseXml(DefaultHandler handler, InputStream is)
                               throws SAXException, IOException, ParserConfigurationException
  {
    SAXParser parser;
    parser = saxFactory.newSAXParser();
    parser.parse(is, handler);
  }
}
