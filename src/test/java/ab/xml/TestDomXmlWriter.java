/*
 * AsoBrain XML Library
 * Copyright (C) 1999-2026 Peter S. Heijnen
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ab.xml;

import java.io.*;
import javax.xml.parsers.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.w3c.dom.*;

/**
 * Test for the {@link DomXmlWriter} class.
 *
 * @author Gerrit Meinders
 */
class TestDomXmlWriter
{
	/**
	 * Tests that the writer can reproduce an XML document that is read using
	 * the standard DOM parser.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	void testDocument1()
		throws Exception
	{
		System.out.println( "========================================================" );
		var readDocument = readDocument( "TestDomXmlWriter.xml" );
		writeAsText( System.out, "read:", readDocument );

		System.out.println( "========================================================" );
		var writer = new DomXmlWriter();
		writeNode( writer, readDocument );
		var writtenDocument = writer.getDocument();
		writeAsText( System.out, "written:", writtenDocument );

		System.out.println( "========================================================" );
		var readText = new StringBuilder();
		writeAsText( readText, "", readDocument );
		var writtenText = new StringBuilder();
		writeAsText( writtenText, "", writtenDocument );
		assertEquals( readText.toString(), writtenText.toString(), "DOM model should unchanged" );
	}

	/**
	 * Read XML file and return it as a DOM document.
	 *
	 * @param path XML file path.
	 *
	 * @return DOM document.
	 *
	 * @throws Exception if the XML document could not be read properly.
	 */
	private static Document readDocument( String path )
		throws Exception
	{
		var dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware( true );
		var db = dbf.newDocumentBuilder();
		return db.parse( TestDomXmlWriter.class.getResourceAsStream( path ) );
	}

	/**
	 * Write DOM node to the specified XML writer.
	 *
	 * @param writer Writes the XML document.
	 * @param node   DOM node to write.
	 *
	 * @throws XMLException if there was a problem writing the XML document.
	 */
	private void writeNode( XMLWriter writer, Node node )
		throws XMLException
	{
		switch ( node.getNodeType() )
		{
			case Node.DOCUMENT_NODE -> writeDocument( writer, (Document)node );
			case Node.ELEMENT_NODE -> writeElement( writer, (Element)node );
			case Node.TEXT_NODE -> writer.text( node.getNodeValue() );
			default -> throw new AssertionError( "Unsupported " + node.getNodeName() );
		}
	}

	/**
	 * Write DOM document node to the specified XML writer.
	 *
	 * @param writer   Writes the XML document.
	 * @param document DOM document node to write.
	 *
	 * @throws XMLException if there was a problem writing the XML document.
	 */
	private void writeDocument( XMLWriter writer, @SuppressWarnings( "TypeMayBeWeakened" ) Document document )
		throws XMLException
	{
		writer.startDocument();
		writeChildNodes( writer, document );
		writer.endDocument();
	}

	/**
	 * Write DOM element node to the specified XML writer.
	 *
	 * @param writer  Writes the XML document.
	 * @param element DOM element node to write.
	 *
	 * @throws XMLException if there was a problem writing the XML document.
	 */
	private void writeElement( XMLWriter writer, @SuppressWarnings( "TypeMayBeWeakened" ) Element element )
		throws XMLException
	{
		var attributes = element.getAttributes();
		if ( attributes != null )
		{
			for ( var i = 0; i < attributes.getLength(); i++ )
			{
				var attribute = attributes.item( i );
				if ( "xmlns".equals( attribute.getPrefix() ) )
				{
					writer.setPrefix( attribute.getLocalName(), attribute.getNodeValue() );
				}
			}
		}

		var childNodes = element.getChildNodes();
		if ( childNodes.getLength() == 0 )
		{
			writer.emptyTag( element.getNamespaceURI(), element.getLocalName() );
		}
		else
		{
			writer.startTag( element.getNamespaceURI(), element.getLocalName() );
		}

		if ( attributes != null )
		{
			for ( var i = 0; i < attributes.getLength(); i++ )
			{
				var attribute = attributes.item( i );
				if ( !"xmlns".equals( attribute.getPrefix() ) )
				{
					writer.attribute( attribute.getNamespaceURI(), attribute.getLocalName(), attribute.getNodeValue() );
				}
			}
		}

		writeChildNodes( writer, element );

		writer.endTag( element.getNamespaceURI(), element.getLocalName() );
	}

	/**
	 * Write child nodes of a DOM node to the specified XML writer.
	 *
	 * @param writer Writes the XML document.
	 * @param node   DOM node whose child nodes to write.
	 *
	 * @throws XMLException if there was a problem writing the XML document.
	 */
	private void writeChildNodes( XMLWriter writer, Node node )
		throws XMLException
	{
		var childNodes = node.getChildNodes();
		for ( var i = 0; i < childNodes.getLength(); i++ )
		{
			writeNode( writer, childNodes.item( i ) );
		}
	}

	/**
	 * Write node as text.
	 *
	 * @param out    Character stream to write output to.
	 * @param indent Indent to use for output.
	 * @param node   Node to write.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	private void writeAsText( Appendable out, String indent, Node node )
		throws IOException
	{
		var isTag = ( node.getLocalName() != null );

		out.append( indent );
		if ( isTag )
		{
			out.append( '<' );
		}
		out.append( node.getNodeName() );

		var attributes = node.getAttributes();
		if ( attributes != null )
		{
			for ( var i = 0; i < attributes.getLength(); i++ )
			{
				var attribute = attributes.item( i );
				out.append( ' ' ).append( attribute.getNodeName() );
				out.append( "=\"" ).append( attribute.getNodeValue() ).append( '"' );
			}
		}

		var nodeValue = node.getNodeValue();
		if ( nodeValue != null )
		{
			var escaped = nodeValue.replace( "\n", "\\n" );
			out.append( " '" ).append( escaped ).append( '\'' );
		}

		if ( isTag )
		{
			out.append( '>' );
		}
		out.append( '\n' );

		var childNodes = node.getChildNodes();
		var childIndent = indent + "    ";

		for ( var i = 0; i < childNodes.getLength(); i++ )
		{
			writeAsText( out, childIndent, childNodes.item( i ) );
		}

		if ( isTag )
		{
			out.append( indent );
			out.append( "</" ).append( node.getNodeName() ).append( ">\n" );
		}
	}
}
