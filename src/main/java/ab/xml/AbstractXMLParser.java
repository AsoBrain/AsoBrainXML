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
import java.util.function.*;
import java.util.stream.*;

import org.jspecify.annotations.*;

/**
 * Provides common functions needed to parse XML using a {@link XMLReader}.
 *
 * @author G. Meinders
 */
@SuppressWarnings( { "unused", "AbstractClassNeverImplemented", "WeakerAccess" } )
public abstract class AbstractXMLParser
{
	protected final XMLReader reader;

	protected AbstractXMLParser( InputStream in, @Nullable String encoding )
		throws XMLException
	{
		var readerFactory = XMLReaderFactory.newInstance();
		reader = readerFactory.createXMLReader( in, encoding );
	}

	protected AbstractXMLParser( XMLReader reader )
	{
		this.reader = reader;
	}

	/**
	 * Skips over the current element.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected void skipElement()
		throws XMLException
	{
		require( XMLEventType.START_ELEMENT );
		var depth = 1;
		do
		{
			switch ( reader.next() )
			{
				case START_ELEMENT -> depth++;
				case END_ELEMENT -> depth--;
				default ->
				{
					// Other event types are ignored while skipping an element.
				}
			}
		}
		while ( depth > 0 );
	}

	/**
	 * Skips over any whitespace at the current event.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected void skipWhitespace()
		throws XMLException
	{
		while ( reader.getEventType() == XMLEventType.CHARACTERS )
		{
			var text = reader.getText();

			var empty = IntStream.range( 0, text.length() ).allMatch( i -> Character.isWhitespace( text.charAt( i ) ) );
			if ( !empty )
			{
				break;
			}

			reader.next();
		}
	}

	/**
	 * Throws an exception if the current event is of a different type than
	 * specified.
	 *
	 * @param eventType Event type.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected void require( XMLEventType eventType )
		throws XMLException
	{
		if ( ( eventType != reader.getEventType() ) )
		{
			var eventQName = getQName();
			throw new XMLException( "Expected %s, but was %s%s".formatted( eventType, reader.getEventType(), eventQName == null ? "" : ' ' + eventQName ) );
		}
	}

	/**
	 * Throws an exception if the current event does not match the specified
	 * event type, namespace URI and local name.
	 *
	 * @param eventType    Event type.
	 * @param namespaceURI Namespace URI.
	 * @param localName    Local name.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected void require( XMLEventType eventType, @Nullable String namespaceURI, String localName )
		throws XMLException
	{
		if ( ( eventType != reader.getEventType() ) ||
		     !matches( namespaceURI, localName ) )
		{
			var eventQName = getQName();
			throw new XMLException( "Expected %s %s, but was %s%s".formatted( eventType, getQName( namespaceURI, localName ), reader.getEventType(), eventQName == null ? "" : ' ' + eventQName ) );
		}
	}

	/**
	 * Throws an exception if the current event does not match the specified
	 * event type, namespace URI and local name.
	 *
	 * @param eventType Event type.
	 * @param localName Local name.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected void require( XMLEventType eventType, String localName )
		throws XMLException
	{
		if ( ( eventType != reader.getEventType() ) || !matches( localName ) )
		{
			var eventQName = getQName();
			throw new XMLException( "Expected %s %s, but was %s%s".formatted( eventType, localName, reader.getEventType(), eventQName == null ? "" : ' ' + eventQName ) );
		}
	}

	/**
	 * Reads the next token if the current token matches the given event type.
	 * If the current token does not match, an exception is thrown.
	 *
	 * @param eventType Event type.
	 *
	 * @throws XMLException if the token does not match, or another XML-related
	 * exception occurs.
	 */
	protected void accept( XMLEventType eventType )
		throws XMLException
	{
		require( eventType );
		reader.next();
	}

	/**
	 * Reads the next token if the current token matches the given event type,
	 * namespace URI and local name. If the current token does not match, an
	 * exception is thrown.
	 *
	 * @param eventType    Event type.
	 * @param namespaceURI Namespace URI.
	 * @param localName    Local name.
	 *
	 * @throws XMLException if the token does not match, or another XML-related
	 * exception occurs.
	 */
	protected void accept( XMLEventType eventType, @Nullable String namespaceURI, String localName )
		throws XMLException
	{
		require( eventType, namespaceURI, localName );
		reader.next();
	}

	/**
	 * Throws an exception indicating that the current token is unexpected.
	 *
	 * @throws XMLException to indicate the current token is unexpected.
	 */
	protected void unexpected()
		throws XMLException
	{
		var eventQName = getQName();
		throw new XMLException( "Unexpected %s%s".formatted( reader.getEventType(), eventQName == null ? "" : " " + eventQName ) );
	}

	/**
	 * Reads tokens until the next start or end of an element.
	 *
	 * @return {@code true} if the current token is the start of an element.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected boolean nextElement()
		throws XMLException
	{
		while ( reader.next() != XMLEventType.END_ELEMENT )
		{
			if ( reader.getEventType() == XMLEventType.START_ELEMENT )
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the current event matches the specified namespace URI and
	 * local name.
	 *
	 * @param localName Local name.
	 *
	 * @return {@code true} if the event matches.
	 */
	protected boolean matches( String localName )
	{
		return localName.equals( reader.getLocalName() );
	}

	/**
	 * Returns whether the current event matches the specified namespace URI and
	 * local name.
	 *
	 * @param namespaceURI Namespace URI.
	 * @param localName    Local name.
	 *
	 * @return {@code true} if the event matches.
	 */
	protected boolean matches( @Nullable String namespaceURI, String localName )
	{
		var readNamespaceURI = reader.getNamespaceURI();
		var readLocalName = reader.getLocalName();
		return ( Objects.equals( namespaceURI, readNamespaceURI ) ) && localName.equals( readLocalName );
	}

	/**
	 * Returns the qualified name for the specified namespace URI and local
	 * name.
	 *
	 * @param namespaceURI Namespace URI.
	 * @param localName    Local name.
	 *
	 * @return Qualified name.
	 */
	protected String getQName( @Nullable String namespaceURI, String localName )
	{
		return namespaceURI == null ? localName : "{%s}:%s".formatted( namespaceURI, localName );
	}

	/**
	 * Returns the qualified name for the current event.
	 *
	 * @return Qualified name; {@code null} if not applicable.
	 */
	protected @Nullable String getQName()
	{
		return switch ( reader.getEventType() )
		{
			case START_ELEMENT, END_ELEMENT -> getQName( reader.getNamespaceURI(), reader.getLocalName() );
			default -> null;
		};
	}

	/**
	 * Returns whether the specified attribute is set.
	 *
	 * @param localName Local name.
	 *
	 * @return Whether the attribute is set.
	 */
	protected boolean isAttributeSet( String localName )
	{
		return isAttributeSet( null, localName );
	}

	/**
	 * Returns whether the specified attribute is set.
	 *
	 * @param namespaceURI Namespace URI.
	 * @param localName    Local name.
	 *
	 * @return Whether the attribute is set.
	 */
	protected boolean isAttributeSet( @Nullable String namespaceURI, String localName )
	{
		return reader.getAttributeValue( namespaceURI, localName ) != null;
	}

	/**
	 * Returns the value of the specified (unqualified) attribute. If the
	 * attribute is not present, an exception is thrown.
	 *
	 * @param localName Local name.
	 *
	 * @return Attribute value.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected String parseAttribute( String localName )
		throws XMLException
	{
		return parseAttribute( null, localName );
	}

	/**
	 * Returns the value of the specified attribute. If the attribute is not
	 * present, an exception is thrown.
	 *
	 * @param namespaceURI Namespace URI.
	 * @param localName    Local name.
	 *
	 * @return Attribute value.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected String parseAttribute( @Nullable String namespaceURI, String localName )
		throws XMLException
	{
		var result = reader.getAttributeValue( namespaceURI, localName );
		if ( result == null )
		{
			throw new XMLException( "Missing required attribute '%s' of element %s.".formatted( localName, getQName() ) );
		}
		return result;
	}

	/**
	 * Returns the double value of the specified attribute. If the attribute is
	 * not present or not a valid double, an exception is thrown.
	 *
	 * @param localName Local name.
	 *
	 * @return Attribute value.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected double parseDoubleAttribute( String localName )
		throws XMLException
	{
		return parseDoubleAttribute( null, localName );
	}

	/**
	 * Returns the double value of the specified attribute. If the attribute is
	 * not present or not a valid double, an exception is thrown.
	 *
	 * @param namespaceURI Namespace URI.
	 * @param localName    Local name.
	 *
	 * @return Attribute value.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected double parseDoubleAttribute( @Nullable String namespaceURI, String localName )
		throws XMLException
	{
		var value = parseAttribute( namespaceURI, localName );
		try
		{
			return Double.parseDouble( value );
		}
		catch ( NumberFormatException ignored )
		{
			throw new XMLException( "Invalid value for attribute '%s': %s".formatted( localName, value ) );
		}
	}

	/**
	 * Returns the integer value of the specified attribute. If the attribute is
	 * not present or not a valid integer, an exception is thrown.
	 *
	 * @param localName Local name.
	 *
	 * @return Attribute value.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected int parseIntegerAttribute( String localName )
		throws XMLException
	{
		return parseIntegerAttribute( null, localName );
	}

	/**
	 * Returns the integer value of the specified attribute. If the attribute is
	 * not present or not a valid integer, an exception is thrown.
	 *
	 * @param namespaceURI Namespace URI.
	 * @param localName    Local name.
	 *
	 * @return Attribute value.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected int parseIntegerAttribute( @Nullable String namespaceURI, String localName )
		throws XMLException
	{
		var value = parseAttribute( namespaceURI, localName );
		try
		{
			return Integer.parseInt( value );
		}
		catch ( NumberFormatException ignored )
		{
			throw new XMLException( "Invalid value for attribute '%s': %s".formatted( localName, value ) );
		}
	}

	/**
	 * Parses character data as a list.
	 *
	 * @param consumer Receives each element of the list as it is parsed.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected void parseList( Consumer<String> consumer )
		throws XMLException
	{
		var builder = new StringBuilder();

		var fromIndex = 0;
		while ( reader.getEventType() == XMLEventType.CHARACTERS )
		{
			builder.delete( 0, fromIndex );
			builder.append( reader.getText() );

			while ( true )
			{
				var toIndex = IntStream.range( fromIndex, builder.length() )
				                       .filter( i -> Character.isWhitespace( builder.charAt( i ) ) )
				                       .findFirst().orElse( -1 );
				if ( toIndex == -1 )
				{
					break;
				}

				if ( toIndex > fromIndex )
				{
					consumer.accept( builder.substring( fromIndex, toIndex ) );
				}

				fromIndex = toIndex + 1;
			}

			reader.next();
		}

		if ( builder.length() > fromIndex )
		{
			consumer.accept( builder.substring( fromIndex ) );
		}
	}

	/**
	 * Parses character data.
	 *
	 * @return Text content.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected String parseTextContent()
		throws XMLException
	{
		var builder = new StringBuilder();

		while ( reader.getEventType() == XMLEventType.CHARACTERS )
		{
			builder.append( reader.getText() );
			reader.next();
		}

		return builder.toString();
	}
}
