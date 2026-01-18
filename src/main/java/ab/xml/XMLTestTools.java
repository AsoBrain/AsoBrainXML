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
import java.util.regex.*;
import javax.xml.stream.*;
import javax.xml.stream.events.*;

import lombok.*;
import org.jspecify.annotations.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tools for testing XML-related code.
 *
 * <p><b>Processing instructions</b>
 *
 * <p>When comparing XML documents, it may not be possible (or practical) for
 * the two XML documents to be identical. For example if the documents include a
 * timestamp. To overcome this, reference documents may be annotated using
 * processing instructions.
 *
 * <p>The syntax for the processing instructions supported by this class is:
 * <pre>&lt;?unit-test instruction [attribute="value" [...]] ?&gt;</pre>
 *
 * <p>For example:
 * <pre>&lt;?unit-test validate type="dateTime" ?&gt;</pre>
 *
 * <p>Available instructions and their attributes are listed below.
 * The term 'content' refers to the text content at the current position in the
 * XML document. Attributes are required, unless specified otherwise.
 *
 * <dl>
 * <dt>Instruction: {@code ignore}</dt>
 * <dd>Ignores the content.</dd>
 * <dt>Instruction: {@code validate}</dt>
 * <dd>Ensures that the content is a valid lexical
 * value for the specified XML Schema data type.</dd>
 * <dt>Attribute: {@code type}</dt>
 * <dd>XML Schema data type, e.g. {@code dateTime}.</dd>
 * </dl>
 *
 * @author G. Meinders
 */
@NoArgsConstructor( access = AccessLevel.PRIVATE )
@SuppressWarnings( { "unused", "squid:S5960" } )
public class XMLTestTools
{
	private static final Pattern PROCESSING_INSTRUCTION_PATTERN = Pattern.compile( "([A-Za-z_][A-Za-z0-9_-]{0,99})\\s{1,999}([A-Za-z_][A-Za-z0-9_-]{0,99})=\"([^\"]{0,99})\"\\s{0,999}" );

	/**
	 * Asserts that the given stream contain an equivalent XML document.
	 *
	 * @param expectedIn Expected XML document.
	 * @param actualIn   Actual XML document.
	 *
	 * @throws XMLStreamException if there is an error with the underlying XML.
	 */
	public static void assertXMLEquals( InputStream expectedIn, InputStream actualIn )
		throws XMLStreamException
	{
		assertXMLEquals( null, expectedIn, actualIn );
	}

	/**
	 * Asserts that the given stream contain an equivalent XML document.
	 *
	 * @param message    Assertion failure message.
	 * @param expectedIn Expected XML document.
	 * @param actualIn   Actual XML document.
	 *
	 * @throws XMLStreamException if there is an error with the underlying XML.
	 */
	public static void assertXMLEquals( @Nullable String message, InputStream expectedIn, InputStream actualIn )
		throws XMLStreamException
	{
		var messagePrefix = ( message != null ) ? message + " - " : "";

		var xmlInputFactory = XMLInputFactory.newFactory();
		xmlInputFactory.setProperty( XMLInputFactory.SUPPORT_DTD, false ); // prevent XXE
		xmlInputFactory.setProperty( XMLInputFactory.IS_COALESCING, Boolean.TRUE );

		var actualReader = xmlInputFactory.createXMLEventReader( actualIn );
		var expectedReader = xmlInputFactory.createXMLEventReader( expectedIn );

		while ( expectedReader.hasNext() && actualReader.hasNext() )
		{
			var expectedEvent = expectedReader.nextEvent();
			var actualEvent = actualReader.nextEvent();

			var location = expectedEvent.getLocation();
			var locationMessagePrefix = "%sLine %d, column %d: ".formatted( messagePrefix, location.getLineNumber(), location.getColumnNumber() );

			if ( handleProcessingInstruction( locationMessagePrefix, expectedEvent, actualEvent ) )
			{
				continue;
			}

			var expectedWriter = new StringWriter();
			expectedEvent.writeAsEncodedUnicode( expectedWriter );
			var expected = expectedWriter.toString();

			var actualWriter = new StringWriter();
			actualEvent.writeAsEncodedUnicode( actualWriter );
			var actual = actualWriter.toString();

			assertEquals( expected, actual, locationMessagePrefix + "Unexpected event." );
			long expected1 = expectedEvent.getEventType();
			assertEquals( expected1, actualEvent.getEventType(), locationMessagePrefix + "Unexpected event type." );
		}

		assertFalse( expectedReader.hasNext(), messagePrefix + "Expected more events." );
		assertFalse( actualReader.hasNext(), messagePrefix + "Expected no more events." );
	}

	private static boolean handleProcessingInstruction( String messagePrefix, XMLEvent expectedEvent, XMLEvent actualEvent )
	{
		if ( expectedEvent.getEventType() != XMLStreamConstants.PROCESSING_INSTRUCTION )
		{
			return false;
		}

		var processingInstruction = (ProcessingInstruction)expectedEvent;
		if ( !"unit-test".equals( processingInstruction.getTarget() ) )
		{
			return false;
		}

		var matcher = PROCESSING_INSTRUCTION_PATTERN.matcher( processingInstruction.getData() );
		assertTrue( matcher.matches(), messagePrefix + "Invalid unit-test processing instruction: " + processingInstruction );

		var instruction = matcher.group( 1 );

		Map<String, String> instructionAttributes = new HashMap<>();
		for ( var i = 2; i < matcher.groupCount(); i += 2 )
		{
			instructionAttributes.put( matcher.group( i ), matcher.group( i + 1 ) );
		}

		switch ( instruction )
		{
			case "ignore" ->
			{
				/* ignore :) */
			}
			case "resource-url" -> processingResourceUrl( messagePrefix, actualEvent, instructionAttributes );
			case "validate" -> processValidate( messagePrefix, actualEvent, instructionAttributes );
			case null,
			     default -> fail( messagePrefix + "Unsupported unit-test processing instruction: " + instruction );
		}

		return true;
	}

	/**
	 * Asserts that the given stream contain an equivalent XML document.
	 *
	 * @param message  Assertion failure message.
	 * @param expected Expected XML document.
	 * @param actual   Actual XML document.
	 *
	 * @throws XMLStreamException if there is an error with the underlying XML.
	 */
	public static void assertXMLEquals( String message, String expected, String actual )
		throws XMLStreamException
	{
		assertXMLEquals( message, new ByteArrayInputStream( expected.getBytes() ), new ByteArrayInputStream( actual.getBytes() ) );
	}

	/**
	 * Handle {@code <?unit-test resource-url name="{name}"
	 * [className="{className}"] ?>} processing instruction.
	 * <p>
	 * This makes sure the XML element content is a reference to the specified
	 * resource. The class name is optional and may be used to specify which
	 * class (loader) is used to find the resource.
	 *
	 * @param messagePrefix         Prefix for assertion failure messages.
	 * @param event                 Current XML event being validated.
	 * @param instructionAttributes Processing instruction attributes.
	 */
	private static void processingResourceUrl( String messagePrefix, XMLEvent event, Map<String, String> instructionAttributes )
	{
		String name = null;
		Class<?> clazz = XMLTestTools.class;

		for ( var attribute : instructionAttributes.entrySet() )
		{
			var attributeName = attribute.getKey();
			var attributeValue = attribute.getValue();

			if ( "name".equals( attributeName ) )
			{
				name = attributeValue;
			}
			else if ( "className".equals( attributeName ) )
			{
				try
				{
					clazz = Class.forName( attributeValue );
				}
				catch ( ClassNotFoundException ignored )
				{
					throw new AssertionError( "Class '%s' not found for '<?unit-test resource-url ?> processing instruction".formatted( attributeValue ) );
				}
			}
			else
			{
				throw new AssertionError( "Unrecognized '%s' attribute for '<?unit-test resource-url ?> processing instruction".formatted( attributeName ) );
			}
		}

		if ( name == null )
		{
			throw new AssertionError( "Missing required 'name' attribute for '<?unit-test resource-url ?> processing instruction" );
		}

		var resourceUrl = clazz.getResource( name );
		assertNotNull( resourceUrl, "%sCan't find resource with name '%s' for class '%s'".formatted( messagePrefix, name, clazz.getName() ) );

		assertEquals( XMLStreamConstants.CHARACTERS, (long)event.getEventType(), messagePrefix + "Unexpected event type." );

		var characters = event.asCharacters();
		var actualUrl = characters.getData();

		Object expected = resourceUrl.toExternalForm();
		assertEquals( expected, actualUrl, "%sInvalid resource URL for name '%s'".formatted( messagePrefix, name ) );
	}

	/**
	 * Handle {@code <?unit-test validate type="{dataType}" ?>}
	 * processing instruction.
	 * <p>
	 * This ensures that the content is a valid lexical value for the specified
	 * XML Schema data type (e.g. {@code dateTime}).
	 *
	 * @param messagePrefix         Prefix for assertion failure messages.
	 * @param event                 Current XML event being validated.
	 * @param instructionAttributes Processing instruction attributes.
	 */
	private static void processValidate( String messagePrefix, XMLEvent event, Map<String, String> instructionAttributes )
	{
		var type = getDataType( instructionAttributes );

		assertEquals( XMLStreamConstants.CHARACTERS, (long)event.getEventType(), messagePrefix + "Unexpected event type." );
		try
		{
			if ( "dateTime".equals( type ) )
			{
				var characters = event.asCharacters();
				DatatypeConverter.parseDateTime( characters.getData() );
			}
			else
			{
				throw new AssertionError( "Validation of data type '%s' is not implemented.".formatted( type ) );
			}
		}
		catch ( Exception e )
		{
			throw new AssertionError( "Invalid value for data type '%s': %s".formatted( type, event ), e );
		}
	}

	private static String getDataType( Map<String, String> instructionAttributes )
	{
		String type = null;

		for ( var attribute : instructionAttributes.entrySet() )
		{
			var attributeName = attribute.getKey();
			var attributeValue = attribute.getValue();

			if ( "type".equals( attributeName ) )
			{
				type = attributeValue;
			}
			else
			{
				throw new AssertionError( "Unrecognized '%s' attribute for '<?unit-test validate ?> processing instruction".formatted( attributeName ) );
			}
		}

		if ( type == null )
		{
			throw new AssertionError( "Missing required attribute 'dataType' for '<?unit-test validate ?> processing instruction" );
		}
		return type;
	}
}
