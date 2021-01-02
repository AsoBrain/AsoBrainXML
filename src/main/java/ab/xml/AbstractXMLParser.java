/*
 * AsoBrain XML Library
 * Copyright (C) 1999-2021 Peter S. Heijnen
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
import java.util.function.*;

import org.jetbrains.annotations.*;

/**
 * Provides common functions needed to parse XML using a {@link XMLReader}.
 *
 * @author G. Meinders
 */
public abstract class AbstractXMLParser
{
	/**
	 * XML reader.
	 */
	protected final XMLReader _reader;

	/**
	 * Constructs a new instance.
	 *
	 * @param in       Stream to read from.
	 * @param encoding Character encoding.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected AbstractXMLParser( @NotNull final InputStream in, @Nullable final String encoding )
	throws XMLException
	{
		final XMLReaderFactory readerFactory = XMLReaderFactory.newInstance();
		_reader = readerFactory.createXMLReader( in, encoding );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param reader XML reader;
	 */
	protected AbstractXMLParser( @NotNull final XMLReader reader )
	{
		_reader = reader;
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
		int depth = 1;
		do
		{
			switch ( _reader.next() )
			{
				case START_ELEMENT:
					depth++;
					break;

				case END_ELEMENT:
					depth--;
					break;
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
		while ( _reader.getEventType() == XMLEventType.CHARACTERS )
		{
			final String text = _reader.getText();

			boolean empty = true;
			for ( int i = 0; i < text.length(); i++ )
			{
				if ( !Character.isWhitespace( text.charAt( i ) ) )
				{
					empty = false;
					break;
				}
			}

			if ( !empty )
			{
				break;
			}

			_reader.next();
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
	protected void require( @NotNull final XMLEventType eventType )
	throws XMLException
	{
		if ( ( eventType != _reader.getEventType() ) )
		{
			final String eventQName = getQName();
			throw new XMLException( "Expected " + eventType + ", but was " + _reader.getEventType() + ( eventQName == null ? "" : ' ' + eventQName ) );
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
	protected void require( @NotNull final XMLEventType eventType, @Nullable final String namespaceURI, @NotNull final String localName )
	throws XMLException
	{
		if ( ( eventType != _reader.getEventType() ) ||
		     !matches( namespaceURI, localName ) )
		{
			final String eventQName = getQName();
			throw new XMLException( "Expected " + eventType + ' ' + getQName( namespaceURI, localName ) + ", but was " + _reader.getEventType() + ( eventQName == null ? "" : ' ' + eventQName ) );
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
	protected void require( @NotNull final XMLEventType eventType, @NotNull final String localName )
	throws XMLException
	{
		if ( ( eventType != _reader.getEventType() ) || !matches( localName ) )
		{
			final String eventQName = getQName();
			throw new XMLException( "Expected " + eventType + ' ' + localName + ", but was " + _reader.getEventType() + ( eventQName == null ? "" : ' ' + eventQName ) );
		}
	}

	/**
	 * Reads the next token if the current token matches the given event type.
	 * If the current token does not match, an exception is thrown.
	 *
	 * @param eventType Event type.
	 *
	 * @throws XMLException if the token does not match, or another XML-related
	 *                      exception occurs.
	 */
	protected void accept( @NotNull final XMLEventType eventType )
	throws XMLException
	{
		require( eventType );
		_reader.next();
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
	 *                      exception occurs.
	 */
	protected void accept( @NotNull final XMLEventType eventType, @Nullable final String namespaceURI, @NotNull final String localName )
	throws XMLException
	{
		require( eventType, namespaceURI, localName );
		_reader.next();
	}

	/**
	 * Throws an exception indicating that the current token is unexpected.
	 *
	 * @throws XMLException to indicate the current token is unexpected.
	 */
	protected void unexpected()
	throws XMLException
	{
		final String eventQName = getQName();
		throw new XMLException( "Unexpected " + _reader.getEventType() + ( eventQName == null ? "" : " " + eventQName ) );
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
		boolean result = false;
		while ( _reader.next() != XMLEventType.END_ELEMENT )
		{
			if ( _reader.getEventType() == XMLEventType.START_ELEMENT )
			{
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Returns whether the current event matches the specified namespace URI and
	 * local name.
	 *
	 * @param localName Local name.
	 *
	 * @return {@code true} if the event matches.
	 */
	protected boolean matches( @NotNull final String localName )
	{
		return localName.equals( _reader.getLocalName() );
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
	protected boolean matches( @Nullable final String namespaceURI, @NotNull final String localName )
	{
		final String readNamespaceURI = _reader.getNamespaceURI();
		final String readLocalName = _reader.getLocalName();
		return ( namespaceURI != null ? namespaceURI.equals( readNamespaceURI ) : readNamespaceURI == null ) && localName.equals( readLocalName );
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
	protected String getQName( @Nullable final String namespaceURI, final String localName )
	{
		return namespaceURI == null ? localName : '{' + namespaceURI + "}:" + localName;
	}

	/**
	 * Returns the qualified name for the current event.
	 *
	 * @return Qualified name; {@code null} if not applicable.
	 */
	@Nullable
	protected String getQName()
	{
		final String result;

		switch ( _reader.getEventType() )
		{
			case START_ELEMENT:
			case END_ELEMENT:
				result = getQName( _reader.getNamespaceURI(), _reader.getLocalName() );
				break;

			default:
				result = null;
				break;
		}

		return result;
	}

	/**
	 * Returns whether the specified attribute is set.
	 *
	 * @param localName Local name.
	 *
	 * @return Whether the attribute is set.
	 */
	protected boolean isAttributeSet( @NotNull final String localName )
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
	protected boolean isAttributeSet( @Nullable final String namespaceURI, @NotNull final String localName )
	{
		return _reader.getAttributeValue( namespaceURI, localName ) != null;
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
	@NotNull
	protected String parseAttribute( @NotNull final String localName )
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
	@NotNull
	protected String parseAttribute( @Nullable final String namespaceURI, @NotNull final String localName )
	throws XMLException
	{
		final String result = _reader.getAttributeValue( namespaceURI, localName );
		if ( result == null )
		{
			throw new XMLException( "Missing required attribute '" + localName + "' of element " + getQName() + '.' );
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
	protected double parseDoubleAttribute( @NotNull final String localName )
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
	protected double parseDoubleAttribute( @Nullable final String namespaceURI, @NotNull final String localName )
	throws XMLException
	{
		final String value = parseAttribute( namespaceURI, localName );
		try
		{
			return Double.parseDouble( value );
		}
		catch ( final NumberFormatException ignored )
		{
			throw new XMLException( "Invalid value for attribute '" + localName + "': " + value );
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
	protected int parseIntegerAttribute( @NotNull final String localName )
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
	protected int parseIntegerAttribute( @Nullable final String namespaceURI, @NotNull final String localName )
	throws XMLException
	{
		final String value = parseAttribute( namespaceURI, localName );
		try
		{
			return Integer.parseInt( value );
		}
		catch ( final NumberFormatException ignored )
		{
			throw new XMLException( "Invalid value for attribute '" + localName + "': " + value );
		}
	}

	/**
	 * Parses character data as a list.
	 *
	 * @param consumer Receives each element of the list as it is parsed.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	protected void parseList( final Consumer<String> consumer )
	throws XMLException
	{
		final StringBuilder builder = new StringBuilder();

		int fromIndex = 0;
		while ( _reader.getEventType() == XMLEventType.CHARACTERS )
		{
			builder.delete( 0, fromIndex );
			builder.append( _reader.getText() );

			int toIndex;

			while ( true )
			{
				// TODO: Other whitespace could be used as well, not just the space character (\u0020).
				toIndex = builder.indexOf( " ", fromIndex );
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

			_reader.next();
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
		final StringBuilder builder = new StringBuilder();

		while ( _reader.getEventType() == XMLEventType.CHARACTERS )
		{
			builder.append( _reader.getText() );
			_reader.next();
		}

		return builder.toString();
	}
}
