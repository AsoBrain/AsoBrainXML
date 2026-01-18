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
import javax.xml.stream.*;

/**
 * XML writer factory for writers that use StAX, the Streaming API for XML.
 *
 * @author G. Meinders
 */
@SuppressWarnings( "unused" )
class StaxWriterFactory
extends XMLWriterFactory
{
	/**
	 * Factory used to created StAX writers.
	 */
	private final XMLOutputFactory factory;

	@SuppressWarnings( "ErrorNotRethrown" )
	StaxWriterFactory()
	{
		try
		{
			factory = XMLOutputFactory.newFactory();
		}
		catch ( FactoryConfigurationError e )
		{
			throw new FactoryException( e );
		}
	}

	@Override
	public XMLWriter createXMLWriter( OutputStream out, String encoding )
	throws XMLException
	{
		XMLStreamWriter writer;
		try
		{
			writer = factory.createXMLStreamWriter( out, encoding );
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}

		if ( isIndenting() )
		{
			var indenting = new IndentingXMLStreamWriter( writer );
			indenting.setNewline( getNewline() );
			indenting.setIndent( getIndent() );
			writer = indenting;
		}

		return new StaxWriter( writer, encoding );
	}

	@Override
	public XMLWriter createXMLWriter( Writer writer, String encoding )
	throws XMLException
	{
		XMLStreamWriter xmlStreamWriter;
		try
		{
			xmlStreamWriter = factory.createXMLStreamWriter( writer );
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}

		if ( isIndenting() )
		{
			var indenting = new IndentingXMLStreamWriter( xmlStreamWriter );
			indenting.setNewline( getNewline() );
			indenting.setIndent( getIndent() );
			xmlStreamWriter = indenting;
		}

		return new StaxWriter( xmlStreamWriter, encoding );
	}
}
