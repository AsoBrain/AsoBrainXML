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

import java.util.*;
import javax.xml.stream.*;

import lombok.*;

/**
 * XML writer implementation that uses StAX, the Streaming API for XML.
 *
 * @author G. Meinders
 */
@RequiredArgsConstructor
@SuppressWarnings( "unused" )
public class StaxWriter
	implements XMLWriter
{
	/**
	 * StAX writer to be used.
	 */
	private final XMLStreamWriter writer;

	/**
	 * Character encoding of the XML document.
	 */
	private final String encoding;

	/**
	 * {@code true} if the writer is currently writing an empty tag.
	 */
	private boolean empty = false;

	/**
	 * Namespace declarations that need to be added to the next start element.
	 */
	private final Collection<NamespaceDeclaration> namespaces = new ArrayList<>();

	@Override
	public void startDocument()
		throws XMLException
	{
		try
		{
			writer.writeStartDocument( encoding, "1.0" );
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void setPrefix( String prefix, String namespaceURI )
		throws XMLException
	{
		try
		{
			writer.setPrefix( prefix, namespaceURI );
			namespaces.add( new NamespaceDeclaration( prefix, namespaceURI ) );
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void startTag( String namespaceURI, String localName )
		throws XMLException
	{
		if ( empty )
		{
			throw new XMLException( "Not allowed inside an empty tag. Use 'endTag' first." );
		}

		try
		{
			if ( namespaceURI == null )
			{
				writer.writeStartElement( localName );
			}
			else
			{
				writer.writeStartElement( namespaceURI, localName );
			}
			for ( NamespaceDeclaration namespace : namespaces )
			{
				writer.writeNamespace( namespace.prefix(), namespace.namespaceURI() );
			}
			namespaces.clear();
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void emptyTag( String namespaceURI, String localName )
		throws XMLException
	{
		try
		{
			if ( namespaceURI == null )
			{
				writer.writeEmptyElement( localName );
			}
			else
			{
				writer.writeEmptyElement( namespaceURI, localName );
			}
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
		empty = true;
	}

	@Override
	public void attribute( String namespaceURI, String localName, String value )
		throws XMLException
	{
		try
		{
			if ( namespaceURI == null )
			{
				writer.writeAttribute( localName, value );
			}
			else
			{
				writer.writeAttribute( namespaceURI, localName, value );
			}
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void text( String characters )
		throws XMLException
	{
		if ( empty )
		{
			throw new XMLException( "Not allowed inside an empty tag. Use 'endTag' first." );
		}

		try
		{
			writer.writeCharacters( characters );
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void endTag( String namespaceURI, String localName )
		throws XMLException
	{
		if ( empty )
		{
			empty = false;
			return;
		}

		try
		{
			writer.writeEndElement();
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void endDocument()
		throws XMLException
	{
		try
		{
			writer.writeEndDocument();
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void flush()
		throws XMLException
	{
		try
		{
			writer.flush();
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}
	}

	/**
	 * Namespace declaration.
	 *
	 * @param prefix       Prefix.
	 * @param namespaceURI Namespace URI.
	 */
	private record NamespaceDeclaration( String prefix, String namespaceURI )
	{
	}
}
