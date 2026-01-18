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

import lombok.*;
import org.jspecify.annotations.*;
import org.xmlpull.v1.*;

/**
 * XML writer implementation that uses XML Pull.
 *
 * @author G. Meinders
 */
@RequiredArgsConstructor( access = AccessLevel.PACKAGE )
@SuppressWarnings( "unused" )
class XmlPullWriter
	implements XMLWriter
{
	/**
	 * XML Pull serializer to be used.
	 */
	private final XmlSerializer serializer;

	/**
	 * Character encoding of the XML document.
	 */
	private final String encoding;

	/**
	 * {@code true} if the writer is currently writing an empty tag.
	 */
	private boolean empty = false;

	@Override
	public void setPrefix( String prefix, String namespaceURI )
		throws XMLException
	{
		try
		{
			serializer.setPrefix( prefix, namespaceURI );
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void startDocument()
		throws XMLException
	{
		try
		{
			serializer.startDocument( encoding, null );
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void startTag( @Nullable String namespaceURI, String localName )
		throws XMLException
	{
		if ( empty )
		{
			throw new XMLException( "Not allowed inside an empty tag. Use 'endTag' first." );
		}

		try
		{
			serializer.startTag( namespaceURI, localName );
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void emptyTag( @Nullable String namespaceURI, String localName )
		throws XMLException
	{
		startTag( namespaceURI, localName );
		empty = true;
	}

	@Override
	public void attribute( @Nullable String namespaceURI, String localName, String value )
		throws XMLException
	{
		try
		{
			serializer.attribute( namespaceURI, localName, value );
		}
		catch ( Exception e )
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
			serializer.text( characters );
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}

	@Override
	public void endTag( @Nullable String namespaceURI, String localName )
		throws XMLException
	{
		if ( empty )
		{
			empty = false;
		}

		try
		{
			serializer.endTag( namespaceURI, localName );
		}
		catch ( Exception e )
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
			serializer.endDocument();
		}
		catch ( Exception e )
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
			serializer.flush();
		}
		catch ( Exception e )
		{
			throw new XMLException( e );
		}
	}
}
