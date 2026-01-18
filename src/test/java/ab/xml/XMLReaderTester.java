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
import java.util.*;

import org.jspecify.annotations.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Provides common unit tests for {@link XMLReader} classes.
 *
 * @author Gerrit Meinders
 */
abstract class XMLReaderTester
{
	/**
	 * Factory that creates the {@code XMLReader}s to be tested.
	 */
	protected XMLReaderFactory factory = null;

	@AfterEach
	public void tearDown()
	{
		factory = null;
	}

	/**
	 * Tests that the reader returns all the appropriate events for the input
	 * document, {@code TestXMLReader-1.xml}. The document for this test should
	 * be small, but should still require all interface methods to be read.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testDocument1()
		throws Exception
	{
		var reader = factory.createXMLReader( Objects.requireNonNull( XMLReaderTester.class.getResourceAsStream( "TestXMLReader-1.xml" ) ), null );
		assertProcessingInstruction( reader, "magic", "processing instruction 1", 2, 35 );
		assertProcessingInstruction( reader, "magic", "", 3, 10 );
		assertEquals( XMLEventType.DTD, ignoreWhiteSpace( reader ), "Unexpected event type." );

		assertStartElement( reader, null, "example" );

		assertStartElement( reader, "https://www.example.com/ns1", "element1",
		                    new Attr( "https://www.example.com/ns1", "attribute1", "value1" ),
		                    new Attr( null, "attribute2", "value2" ) );

		assertStartElement( reader, "https://www.example.com/default", "element2",
		                    new Attr( null, "attribute3", "value3" ) );
		assertEquals( 11, reader.getLineNumber(), "Unexpected line number" );
		assertEquals( 35, reader.getColumnNumber(), "Unexpected column number" );

		assertEndElement( reader, "https://www.example.com/default", "element2" );

		assertStartElement( reader, "https://www.example.com/ns1", "element3",
		                    new Attr( "https://www.example.com/ns2", "attribute4", "value4" ),
		                    new Attr( null, "attribute5", "ns2:value5" ) );
		readNextCharacters( reader, "world" );
		assertProcessingInstruction( reader, "magic", "processing instruction 2", 14, 46 );
		assertEndElement( reader, "https://www.example.com/ns1", "element3" );

		assertStartElement( reader, "https://www.example.com/ns2", "element4" );
		assertEndElement( reader, "https://www.example.com/ns2", "element4" );

		assertEndElement( reader, "https://www.example.com/ns1", "element1" );

		readNextCharacters( reader, "<entity references>" );

		assertStartElement( reader, "https://www.example.com/ns2", "element5",
		                    new Attr( null, "attribute6", "value6" ),
		                    new Attr( "https://www.example.com/ns2", "attribute7", "value7" ) );
		assertEndElement( reader, "https://www.example.com/ns2", "element5" );

		assertEndElement( reader, null, "example" );

		assertEquals( XMLEventType.END_DOCUMENT, ignoreWhiteSpace( reader ), "Unexpected event type." );
	}

	private void assertProcessingInstruction( XMLReader reader, String expectedTarget, String expectedData, int lineNumber, int columnNumber )
		throws XMLException
	{
		assertEquals( XMLEventType.PROCESSING_INSTRUCTION, ignoreWhiteSpace( reader ), "Unexpected event type." );
		assertEquals( expectedTarget, reader.getPITarget(), "Unexpected processing instruction target." );
		assertEquals( expectedData, reader.getPIData().trim(), "Unexpected processing instruction data." );
		assertEquals( lineNumber, reader.getLineNumber(), "Unexpected line number" );
		assertEquals( columnNumber, reader.getColumnNumber(), "Unexpected column number" );
	}

	record Attr( @Nullable String namespaceURI, String name, String value )
	{
	}

	private void assertStartElement( XMLReader reader, @Nullable String namespaceURI, String tagName, Attr... attributes )
		throws XMLException
	{
		readNext( reader, XMLEventType.START_ELEMENT, true );
		assertEquals( namespaceURI, reader.getNamespaceURI(), "Unexpected namespace URI." );
		assertEquals( tagName, reader.getLocalName(), "Unexpected local name." );
		assertEquals( attributes.length, reader.getAttributeCount(), "Unexpected attribute count." );

		for ( int i = 0; i < attributes.length; i++ )
		{
			assertAttribute( reader, i, attributes[ i ].namespaceURI(), attributes[ i ].name(), attributes[ i ].value() );
		}
	}

	private void assertAttribute( XMLReader reader, int index, @Nullable String namespaceURI, String name, String expectedValue )
	{
		assertEquals( namespaceURI, reader.getAttributeNamespaceURI( index ), "Unexpected namespace URI for attribute " + index + "." );
		assertEquals( name, reader.getAttributeLocalName( index ), "Unexpected local name for attribute " + index + "." );
		assertEquals( expectedValue, reader.getAttributeValue( index ), "Unexpected value for attribute " + index + "." );
		assertEquals( expectedValue, reader.getAttributeValue( namespaceURI, name ), "Unexpected value for attribute " + index + "." );
	}

	private void assertEndElement( XMLReader reader, @Nullable String namespaceURI, String tagName )
		throws XMLException
	{
		readNext( reader, XMLEventType.END_ELEMENT, true );
		assertEquals( namespaceURI, reader.getNamespaceURI(), "Unexpected namespace URI." );
		assertEquals( tagName, reader.getLocalName(), "Unexpected local name." );
	}

	/**
	 * Tests that the reader returns all the appropriate events for the input
	 * document, {@code TestXMLReader-2.xml}. This test focuses on coalescing of
	 * character events using a simple XHTML document with 5 paragraphs of
	 * "Lorem Ipsum".
	 *
	 * @throws XMLException if the test fails.
	 */
	@Test
	public void testDocument2()
		throws XMLException
	{
		var reader = factory.createXMLReader( Objects.requireNonNull( TestStaxReader.class.getResourceAsStream( "TestXMLReader-2.xml" ) ), null );

		assertEquals( XMLEventType.START_ELEMENT, ignoreWhiteSpace( reader ), "Unexpected event type." );
		assertEquals( "http://www.w3.org/1999/xhtml", reader.getNamespaceURI(), "Unexpected namespace URI." );
		assertEquals( "html", reader.getLocalName(), "Unexpected local name." );
		assertEquals( 1, reader.getAttributeCount(), "Unexpected attribute count." );
		assertAttribute( reader, 0, "http://www.w3.org/XML/1998/namespace", "lang", "la" );

		readNextXhtmlTag( reader, XMLEventType.START_ELEMENT, "head" );
		assertEquals( 0, reader.getAttributeCount(), "Unexpected attribute count." );

		readNextXhtmlTagWithTextContent( reader, "title", "Lorem Ipsum" );

		readNextXhtmlTag( reader, XMLEventType.END_ELEMENT, "head" );

		readNextXhtmlTag( reader, XMLEventType.START_ELEMENT, "body" );
		assertEquals( 0, reader.getAttributeCount(), "Unexpected attribute count." );

		readNextXhtmlTagWithTextContent( reader, "p", """
			Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam
			\t\t\tvehicula fermentum nibh, at placerat tellus hendrerit at. Quisque ac
			\t\t\tarcu vel libero suscipit posuere. Sed mauris est, tristique egestas
			\t\t\tmattis a, convallis et massa. Aliquam non sapien in augue dapibus
			\t\t\tcursus. Cum sociis natoque penatibus et magnis dis parturient
			\t\t\tmontes, nascetur ridiculus mus. Fusce in aliquet eros. Sed vitae
			\t\t\ttortor eu neque varius accumsan vitae rhoncus est. Praesent in nisl
			\t\t\turna. Curabitur rhoncus lacinia tellus, eu pretium nulla vulputate
			\t\t\tsit amet. Maecenas laoreet, sem fringilla laoreet consequat, nibh
			\t\t\tsapien fringilla odio, sed hendrerit magna massa vel urna. Cras
			\t\t\ttempor, turpis in tempus ullamcorper, velit nisl eleifend lorem, sit
			\t\t\tamet ultricies erat tellus nec purus. Mauris consectetur sodales
			\t\t\tnunc, vitae varius augue blandit ut. Ut congue, sapien non
			\t\t\tsollicitudin adipiscing, nisi nisl ultricies lectus, quis fermentum
			\t\t\torci tellus vel tortor.""" );

		readNextXhtmlTagWithTextContent( reader, "p", """
			Pellentesque id metus lorem, vel suscipit ipsum. Curabitur
			\t\t\tscelerisque consequat dictum. Pellentesque habitant morbi tristique
			\t\t\tsenectus et netus et malesuada fames ac turpis egestas. Ut vulputate
			\t\t\tneque et sapien auctor semper accumsan dui aliquet. Cum sociis
			\t\t\tnatoque penatibus et magnis dis parturient montes, nascetur
			\t\t\tridiculus mus. Vivamus aliquet odio sed sem rutrum posuere. Maecenas
			\t\t\tsuscipit tortor lorem, eget porta erat. Donec ac nibh a massa
			\t\t\tdapibus tempus sed eu lorem. Maecenas et est eros. Nunc feugiat dui
			\t\t\tfringilla purus gravida egestas. Pellentesque habitant morbi
			\t\t\ttristique senectus et netus et malesuada fames ac turpis egestas.""" );

		readNextXhtmlTagWithTextContent( reader, "p", """
			Pellentesque habitant morbi tristique senectus et netus et malesuada
			\t\t\tfames ac turpis egestas. Donec fermentum justo ut dolor dictum
			\t\t\ttincidunt aliquam mauris egestas. Donec eget eros augue, sed tempus
			\t\t\tdolor. Integer elit est, porttitor id rutrum sed, adipiscing nec
			\t\t\tnisi. Vivamus turpis lectus, egestas sed faucibus eu, ullamcorper
			\t\t\teget dolor. Sed vitae sem ac sapien pulvinar dapibus. Vestibulum
			\t\t\tlobortis, ligula vel condimentum rutrum, nunc nisl sollicitudin
			\t\t\tmetus, a cursus turpis orci et mauris. Nunc quis mauris elit, quis
			\t\t\tsodales justo.""" );

		readNextXhtmlTagWithTextContent( reader, "p", """
			Morbi eu velit eu sem viverra laoreet. Phasellus luctus lacinia
			\t\t\tmauris in ultrices. Integer at eros ac arcu malesuada sodales ac nec
			\t\t\tenim. Vivamus at arcu ut massa volutpat ultrices. Praesent rhoncus
			\t\t\trutrum lorem ut placerat. Nam imperdiet sapien ac sem porta
			\t\t\tvolutpat. Nam lobortis eleifend lectus vel lacinia. Ut massa sapien,
			\t\t\tsagittis ac ornare non, lobortis at dui.""" );

		readNextXhtmlTagWithTextContent( reader, "p", """
			Integer ut elit a urna interdum facilisis. Quisque in odio at lacus
			\t\t\tsodales porta. Mauris aliquam sodales tellus at scelerisque. Nulla
			\t\t\tfacilisi. Nunc cursus feugiat turpis, vitae faucibus urna
			\t\t\tpellentesque id. Suspendisse volutpat tortor sit amet dui adipiscing
			\t\t\tnec hendrerit mi tempor. Maecenas a libero et orci varius pharetra
			\t\t\ttincidunt at magna. Nullam a massa ipsum. Quisque placerat, sapien
			\t\t\tvitae mattis ornare, neque mi luctus lacus, sed tincidunt leo erat
			\t\t\teget dui. Sed gravida mi tellus, sed consequat mi. Curabitur
			\t\t\tsollicitudin placerat arcu ultrices scelerisque. Maecenas non arcu
			\t\t\tvitae lorem dictum aliquam. Donec vitae est et orci posuere
			\t\t\tscelerisque.""" );

		readNextXhtmlTag( reader, XMLEventType.END_ELEMENT, "body" );

		readNextXhtmlTag( reader, XMLEventType.END_ELEMENT, "html" );

		assertEquals( XMLEventType.END_DOCUMENT, ignoreWhiteSpace( reader ), "Unexpected event type." );
	}

	private void readNextXhtmlTagWithTextContent( XMLReader reader, String tagName, String text )
		throws XMLException
	{
		readNextXhtmlTag( reader, XMLEventType.START_ELEMENT, tagName );
		assertEquals( 0, reader.getAttributeCount(), "Unexpected attribute count." );
		readNextCharacters( reader, text );
		readNextXhtmlTag( reader, XMLEventType.END_ELEMENT, tagName );
	}

	private void readNextXhtmlTag( XMLReader reader, XMLEventType expectedType, String expectedName )
		throws XMLException
	{
		assertEquals( expectedType, ignoreWhiteSpace( reader ), "Unexpected event type." );
		assertEquals( "http://www.w3.org/1999/xhtml", reader.getNamespaceURI(), "Unexpected namespace URI." );
		assertEquals( expectedName, reader.getLocalName(), "Unexpected local name." );
	}

	private void readNextCharacters( XMLReader reader, String expected )
		throws XMLException
	{
		readNext( reader, XMLEventType.CHARACTERS, false );
		assertEquals( expected, reader.getText().trim(), "Unexpected character data." );
	}

	/*
	 * Test exception handling as specified by the {@link XMLReader} interface.
	 * This includes calling methods for event types that are not allowed and
	 * illegal arguments provided to methods. Exceptions relating to the
	 * underlying XML API and I/O (represented by {@link XMLException}) are not
	 * tested.
	 */

	@Test
	public void testExceptionHandling_startDocument()
		throws Exception
	{
		var reader = createReaderForContent( "<?xml version='1.0'?><!DOCTYPE root><root>characters<?pi?></root>" );
		assertEquals( XMLEventType.START_DOCUMENT, reader.getEventType(), "Unexpected event type." );

		assertThrows( IllegalStateException.class, reader::getNamespaceURI );
		assertThrows( IllegalStateException.class, reader::getLocalName );
		assertThrows( IllegalStateException.class, reader::getAttributeCount );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeNamespaceURI( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeLocalName( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( null, "attribute" ) );
		assertThrows( IllegalStateException.class, reader::getText );
		assertThrows( IllegalStateException.class, reader::getPITarget );
		assertThrows( IllegalStateException.class, reader::getPIData );
	}

	@Test
	public void testExceptionHandling_DTD()
		throws Exception
	{
		var reader = createReaderForContent( "<?xml version='1.0'?><!DOCTYPE root><root>characters<?pi?></root>" );
		assertEquals( XMLEventType.START_DOCUMENT, reader.getEventType(), "Unexpected event type." );
		readNext( reader, XMLEventType.DTD, false );

		assertThrows( IllegalStateException.class, reader::getNamespaceURI );
		assertThrows( IllegalStateException.class, reader::getLocalName );
		assertThrows( IllegalStateException.class, reader::getAttributeCount );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeNamespaceURI( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeLocalName( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( null, "attribute" ) );
		assertThrows( IllegalStateException.class, reader::getText );
		assertThrows( IllegalStateException.class, reader::getPITarget );
		assertThrows( IllegalStateException.class, reader::getPIData );
	}

	@Test
	public void testExceptionHandling_startElement()
		throws Exception
	{
		var reader = createReaderForContent( "<?xml version='1.0'?><!DOCTYPE root><root>characters<?pi?></root>" );
		assertEquals( XMLEventType.START_DOCUMENT, reader.getEventType(), "Unexpected event type." );
		readNext( reader, XMLEventType.DTD, false );
		readNext( reader, XMLEventType.START_ELEMENT, false );

		assertNull( reader.getNamespaceURI(), "Unexpected namespace URI." );
		assertEquals( "root", reader.getLocalName(), "Unexpected local name." );
		assertEquals( 0, reader.getAttributeCount(), "Unexpected attribute count." );
		assertThrows( IndexOutOfBoundsException.class, () -> reader.getAttributeNamespaceURI( 0 ) );
		assertThrows( IndexOutOfBoundsException.class, () -> reader.getAttributeLocalName( 0 ) );
		assertThrows( IndexOutOfBoundsException.class, () -> reader.getAttributeValue( 0 ) );
		assertNull( reader.getAttributeValue( null, "attribute" ), "Value of non-existent attribute should be null." );
		assertThrows( IllegalStateException.class, reader::getText );
		assertThrows( IllegalStateException.class, reader::getPITarget );
		assertThrows( IllegalStateException.class, reader::getPIData );
	}

	@Test
	public void testExceptionHandling_characters()
		throws Exception
	{
		var reader = createReaderForContent( "<?xml version='1.0'?><!DOCTYPE root><root>characters<?pi?></root>" );
		assertEquals( XMLEventType.START_DOCUMENT, reader.getEventType(), "Unexpected event type." );
		readNext( reader, XMLEventType.DTD, false );
		readNext( reader, XMLEventType.START_ELEMENT, false );
		readNext( reader, XMLEventType.CHARACTERS, false );

		assertThrows( IllegalStateException.class, reader::getNamespaceURI );
		assertThrows( IllegalStateException.class, reader::getLocalName );
		assertThrows( IllegalStateException.class, reader::getAttributeCount );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeNamespaceURI( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeLocalName( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( null, "attribute" ) );
		assertEquals( "characters", reader.getText(), "Unexpected character data." );
		assertThrows( IllegalStateException.class, reader::getPITarget );
		assertThrows( IllegalStateException.class, reader::getPIData );
	}

	@Test
	public void testExceptionHandling_processingInstruction()
		throws Exception
	{
		var reader = createReaderForContent( "<?xml version='1.0'?><!DOCTYPE root><root>characters<?pi?></root>" );
		assertEquals( XMLEventType.START_DOCUMENT, reader.getEventType(), "Unexpected event type." );
		readNext( reader, XMLEventType.DTD, false );
		readNext( reader, XMLEventType.START_ELEMENT, false );
		readNext( reader, XMLEventType.CHARACTERS, false );
		readNext( reader, XMLEventType.PROCESSING_INSTRUCTION, false );

		assertThrows( IllegalStateException.class, reader::getNamespaceURI );
		assertThrows( IllegalStateException.class, reader::getLocalName );
		assertThrows( IllegalStateException.class, reader::getAttributeCount );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeNamespaceURI( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeLocalName( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( null, "attribute" ) );
		assertThrows( IllegalStateException.class, reader::getText );
		assertEquals( "pi", reader.getPITarget(), "Unexpected processing instruction target." );
		assertEquals( "", reader.getPIData(), "Unexpected processing instruction data." );
	}

	@Test
	public void testExceptionHandling_endElement()
		throws Exception
	{
		var reader = createReaderForContent( "<?xml version='1.0'?><!DOCTYPE root><root>characters<?pi?></root>" );
		assertEquals( XMLEventType.START_DOCUMENT, reader.getEventType(), "Unexpected event type." );
		readNext( reader, XMLEventType.DTD, false );
		readNext( reader, XMLEventType.START_ELEMENT, false );
		readNext( reader, XMLEventType.CHARACTERS, false );
		readNext( reader, XMLEventType.PROCESSING_INSTRUCTION, false );
		readNext( reader, XMLEventType.END_ELEMENT, false );

		assertNull( reader.getNamespaceURI(), "Unexpected namespace URI." );
		assertEquals( "root", reader.getLocalName(), "Unexpected local name." );
		assertThrows( IllegalStateException.class, reader::getAttributeCount );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeNamespaceURI( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeLocalName( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( null, "attribute" ) );
		assertThrows( IllegalStateException.class, reader::getText );
		assertThrows( IllegalStateException.class, reader::getPITarget );
		assertThrows( IllegalStateException.class, reader::getPIData );
	}

	@Test
	public void testExceptionHandling_endDocument()
		throws Exception
	{
		var reader = createReaderForContent( "<?xml version='1.0'?><!DOCTYPE root><root>characters<?pi?></root>" );
		assertEquals( XMLEventType.START_DOCUMENT, reader.getEventType(), "Unexpected event type." );
		readNext( reader, XMLEventType.DTD, false );
		readNext( reader, XMLEventType.START_ELEMENT, false );
		readNext( reader, XMLEventType.CHARACTERS, false );
		readNext( reader, XMLEventType.PROCESSING_INSTRUCTION, false );
		readNext( reader, XMLEventType.END_ELEMENT, false );
		readNext( reader, XMLEventType.END_DOCUMENT, false );

		assertThrows( IllegalStateException.class, reader::next );
		assertThrows( IllegalStateException.class, reader::getNamespaceURI );
		assertThrows( IllegalStateException.class, reader::getLocalName );
		assertThrows( IllegalStateException.class, reader::getAttributeCount );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeNamespaceURI( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeLocalName( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( 0 ) );
		assertThrows( IllegalStateException.class, () -> reader.getAttributeValue( null, "attribute" ) );
		assertThrows( IllegalStateException.class, reader::getText );
		assertThrows( IllegalStateException.class, reader::getPITarget );
		assertThrows( IllegalStateException.class, reader::getPIData );
	}

	private void readNext( XMLReader reader, XMLEventType expectType, boolean ignoreWhiteSpace )
		throws XMLException
	{
		assertEquals( expectType, ignoreWhiteSpace ? ignoreWhiteSpace( reader ) : reader.next(), "Unexpected event type." );
	}

	/**
	 * Creates a reader to read the given XML document. This method always uses
	 * UTF-8 character encoding.
	 *
	 * @param document XML document.
	 *
	 * @return XML reader.
	 *
	 * @throws XMLException if an error occurs.
	 */
	protected XMLReader createReaderForContent( String document )
		throws XMLException
	{
		return createReaderForContent( document, "UTF-8" );
	}

	/**
	 * Creates a reader to read the given XML document.
	 *
	 * @param document XML document.
	 * @param encoding Character encoding.
	 *
	 * @return XML reader.
	 *
	 * @throws XMLException if an error occurs.
	 */
	protected XMLReader createReaderForContent( String document, String encoding )
		throws XMLException
	{
		try
		{
			var in = new ByteArrayInputStream( document.getBytes( encoding ) );
			return factory.createXMLReader( in, encoding );
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new AssertionError( e );
		}
	}

	/**
	 * Returns the next event from the given reader, optionally skipping one or
	 * more character event that consists only of whitespace.
	 *
	 * <p>Example:
	 * <pre>
	 * XMLReader reader;
	 * XMLEventType nonWhitespace = ignoreWhiteSpace( reader );
	 * </pre>
	 *
	 * @param reader Reader to be used.
	 *
	 * @return Current event type.
	 *
	 * @throws XMLException if the next event can't be read.
	 */
	protected XMLEventType ignoreWhiteSpace( XMLReader reader )
		throws XMLException
	{
		var result = reader.next();
		while ( result == XMLEventType.CHARACTERS && reader.getText().isBlank() )
		{
			result = reader.next();
		}
		return result;
	}
}
