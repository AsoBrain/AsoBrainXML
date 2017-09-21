/*
 * AsoBrain XML Library
 * Copyright (C) 1999-2017 Peter S. Heijnen
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
	 * Returns whether the current event matches the specified namespace URI and
	 * local name.
	 *
	 * @param localName Local name.
	 *
	 * @return {@code true} if the event matches.
	 */
	protected boolean matches( final String localName )
	{
		final String readLocalName = _reader.getLocalName();
		return ( localName != null ) ? localName.equals( readLocalName ) : ( readLocalName == null );
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
	protected boolean matches( @Nullable final String namespaceURI, final String localName )
	{
		final String readNamespaceURI = _reader.getNamespaceURI();
		final String readLocalName = _reader.getLocalName();
		return ( ( namespaceURI != null ) ? namespaceURI.equals( readNamespaceURI ) : ( readNamespaceURI == null ) ) &&
		       ( ( localName != null ) ? localName.equals( readLocalName ) : ( readLocalName == null ) );
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
}
