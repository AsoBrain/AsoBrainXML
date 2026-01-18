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

import javax.xml.stream.*;

/**
 * XML reader implementation that uses StAX, the Streaming API for XML.
 *
 * @author Gerrit Meinders
 */
class StaxReader
implements XMLReader
{
	/**
	 * StAX reader to be used.
	 */
	private final XMLStreamReader reader;

	/**
	 * Event type returned by the last call to {@link #next()}.
	 */
	private XMLEventType eventType;

	StaxReader( XMLStreamReader reader )
	{
		this.reader = reader;
		eventType = XMLEventType.START_DOCUMENT;
	}

	@Override
	public XMLEventType getEventType()
	{
		return eventType;
	}

	@Override
	public XMLEventType next()
	throws XMLException
	{
		if ( eventType == XMLEventType.END_DOCUMENT )
		{
			throw new IllegalStateException( "Not allowed after %s event.".formatted( XMLEventType.END_DOCUMENT ) );
		}

		try
		{
			if ( !reader.hasNext() )
			{
				throw new XMLException( "Unexpected end of document." );
			}
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}

		XMLEventType result;
		do
		{
			int type;
			try
			{
				type = reader.next();
			}
			catch ( XMLStreamException e )
			{
				throw new XMLException( e );
			}

			result = switch ( type )
			{
				case XMLStreamConstants.START_ELEMENT -> XMLEventType.START_ELEMENT;
				case XMLStreamConstants.END_ELEMENT -> XMLEventType.END_ELEMENT;
				case XMLStreamConstants.PROCESSING_INSTRUCTION -> XMLEventType.PROCESSING_INSTRUCTION;
				case XMLStreamConstants.CHARACTERS,
				     XMLStreamConstants.CDATA,
				     XMLStreamConstants.ENTITY_REFERENCE,
				     XMLStreamConstants.SPACE -> XMLEventType.CHARACTERS;
				case XMLStreamConstants.COMMENT,
				     XMLStreamConstants.ENTITY_DECLARATION,
				     XMLStreamConstants.NOTATION_DECLARATION,
				     XMLStreamConstants.NAMESPACE,
				     XMLStreamConstants.ATTRIBUTE -> null;
				case XMLStreamConstants.START_DOCUMENT -> XMLEventType.START_DOCUMENT;
				case XMLStreamConstants.END_DOCUMENT -> XMLEventType.END_DOCUMENT;
				case XMLStreamConstants.DTD -> XMLEventType.DTD;
				default -> throw new XMLException( "Unknown event type: " + type );
			};
		}
		while ( result == null );

		eventType = result;

		return result;
	}

	@Override
	public String getNamespaceURI()
	{
		if ( ( eventType != XMLEventType.START_ELEMENT ) &&
		     ( eventType != XMLEventType.END_ELEMENT ) )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return reader.getNamespaceURI();
	}

	@Override
	public String getLocalName()
	{
		if ( ( eventType != XMLEventType.START_ELEMENT ) &&
		     ( eventType != XMLEventType.END_ELEMENT ) )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return reader.getLocalName();
	}

	@Override
	public int getAttributeCount()
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return reader.getAttributeCount();
	}

	@Override
	public String getAttributeNamespaceURI( int index )
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( ( index < 0 ) || ( index >= getAttributeCount() ) )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ')' );
		}

		return reader.getAttributeNamespace( index );
	}

	@Override
	public String getAttributeLocalName( int index )
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( ( index < 0 ) || ( index >= getAttributeCount() ) )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ')' );
		}

		return reader.getAttributeLocalName( index );
	}

	@Override
	public String getAttributeValue( int index )
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		if ( ( index < 0 ) || ( index >= getAttributeCount() ) )
		{
			throw new IndexOutOfBoundsException( index + " (attributeCount: " + getAttributeCount() + ')' );
		}

		return reader.getAttributeValue( index );
	}

	@Override
	public String getAttributeValue( String localName )
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		String result = null;

		var attributeCount = reader.getAttributeCount();
		for ( var i = 0; i < attributeCount; i++ )
		{
			if ( localName.equals( reader.getAttributeLocalName( i ) ) )
			{
				result = reader.getAttributeValue( i );
				break;
			}
		}

		return result;
	}

	@Override
	public String getAttributeValue( String namespaceURI, String localName )
	{
		if ( eventType != XMLEventType.START_ELEMENT )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return reader.getAttributeValue( namespaceURI, localName );
	}

	@Override
	public String getText()
	{
		if ( eventType != XMLEventType.CHARACTERS )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return reader.getText();
	}

	@Override
	public String getPITarget()
	{
		if ( eventType != XMLEventType.PROCESSING_INSTRUCTION )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return reader.getPITarget();
	}

	@Override
	public String getPIData()
	{
		if ( eventType != XMLEventType.PROCESSING_INSTRUCTION )
		{
			throw new IllegalStateException( "Not allowed for " + eventType );
		}

		return reader.getPIData();
	}

	@Override
	public int getLineNumber()
	{
		return reader.getLocation().getLineNumber();
	}

	@Override
	public int getColumnNumber()
	{
		return reader.getLocation().getColumnNumber();
	}
}
