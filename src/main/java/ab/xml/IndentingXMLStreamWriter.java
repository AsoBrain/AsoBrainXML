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

import javax.xml.namespace.*;
import javax.xml.stream.*;

import lombok.*;

/**
 * Wraps an underlying {@link XMLStreamWriter} to automatically add newlines and
 * indenting for elements. Elements that do no contain nested elements are
 * themselves indented, but have no newlines and indenting added around their
 * content.
 *
 * @author G. Meinders
 */
@RequiredArgsConstructor
@SuppressWarnings( "unused" )
public class IndentingXMLStreamWriter
	implements XMLStreamWriter
{
	/**
	 * Underlying writer.
	 */
	private final XMLStreamWriter writer;

	/**
	 * Current nesting depth, used for indenting.
	 */
	private int depth = 0;

	/**
	 * String inserted as a newline.  The default is {@code "\n"}.
	 */
	@Getter
	@Setter
	private String newline = "\n";

	/**
	 * String inserted for each level of indenting. The default is {@code
	 * "\t"}.
	 */
	@Getter
	@Setter
	private String indent = "\t";

	/**
	 * Whether the current node contains no elements (like a
	 * 'xsd:simpleType').
	 */
	private boolean simpleType = true;

	/**
	 * Starts a new line with the proper amount of indenting and increases the
	 * nesting depth.
	 *
	 * @throws XMLStreamException if the whitespace can't be written to the
	 * underlying stream.
	 */
	private void indentIn()
		throws XMLStreamException
	{
		writer.writeCharacters( getNewline() );
		var curDepth = depth++;
		for ( var i = 0; i < curDepth; i++ )
		{
			writer.writeCharacters( getIndent() );
		}
		simpleType = true;
	}

	/**
	 * Starts a new line with the proper amount of indenting, but does not
	 * change the nesting depth. This is used for empty tags.
	 *
	 * @throws XMLStreamException if the whitespace can't be written to the
	 * underlying stream.
	 */
	private void indentSame()
		throws XMLStreamException
	{
		writer.writeCharacters( getNewline() );
		var curDepth = depth;
		for ( var i = 0; i < curDepth; i++ )
		{
			writer.writeCharacters( getIndent() );
		}
		simpleType = false;
	}

	/**
	 * Decreases the nesting depth, and starts a new line with the proper amount
	 * of indenting (except for a {@link #simpleType}).
	 *
	 * @throws XMLStreamException if the whitespace can't be written to the
	 * underlying stream.
	 */
	private void indentOut()
		throws XMLStreamException
	{
		var curDepth = --depth;
		if ( !simpleType )
		{
			writer.writeCharacters( getNewline() );
			for ( var i = 0; i < curDepth; i++ )
			{
				writer.writeCharacters( getIndent() );
			}
		}
		simpleType = false;
	}

	@Override
	public void writeStartElement( String localName )
		throws XMLStreamException
	{
		indentIn();
		writer.writeStartElement( localName );
	}

	@Override
	public void writeStartElement( String namespaceURI, String localName )
		throws XMLStreamException
	{
		indentIn();
		writer.writeStartElement( namespaceURI, localName );
	}

	@Override
	public void writeStartElement( String prefix, String localName, String namespaceURI )
		throws XMLStreamException
	{
		indentIn();
		writer.writeStartElement( prefix, localName, namespaceURI );
	}

	@Override
	public void writeEmptyElement( String namespaceURI, String localName )
		throws XMLStreamException
	{
		indentSame();
		writer.writeEmptyElement( namespaceURI, localName );
	}

	@Override
	public void writeEmptyElement( String prefix, String localName, String namespaceURI )
		throws XMLStreamException
	{
		indentSame();
		writer.writeEmptyElement( prefix, localName, namespaceURI );
	}

	@Override
	public void writeEmptyElement( String localName )
		throws XMLStreamException
	{
		indentSame();
		writer.writeEmptyElement( localName );
	}

	@Override
	public void writeEndElement()
		throws XMLStreamException
	{
		indentOut();
		writer.writeEndElement();
	}

	@Override
	public void writeEndDocument()
		throws XMLStreamException
	{
		writer.writeEndDocument();
	}

	@Override
	public void close()
		throws XMLStreamException
	{
		writer.close();
	}

	@Override
	public void flush()
		throws XMLStreamException
	{
		writer.flush();
	}

	@Override
	public void writeAttribute( String localName, String value )
		throws XMLStreamException
	{
		writer.writeAttribute( localName, value );
	}

	@Override
	public void writeAttribute( String prefix, String namespaceURI, String localName, String value )
		throws XMLStreamException
	{
		writer.writeAttribute( prefix, namespaceURI, localName, value );
	}

	@Override
	public void writeAttribute( String namespaceURI, String localName, String value )
		throws XMLStreamException
	{
		writer.writeAttribute( namespaceURI, localName, value );
	}

	@Override
	public void writeNamespace( String prefix, String namespaceURI )
		throws XMLStreamException
	{
		writer.writeNamespace( prefix, namespaceURI );
	}

	@Override
	public void writeDefaultNamespace( String namespaceURI )
		throws XMLStreamException
	{
		writer.writeDefaultNamespace( namespaceURI );
	}

	@Override
	public void writeComment( String data )
		throws XMLStreamException
	{
		writer.writeComment( data );
	}

	@Override
	public void writeProcessingInstruction( String target )
		throws XMLStreamException
	{
		writer.writeProcessingInstruction( target );
	}

	@Override
	public void writeProcessingInstruction( String target, String data )
		throws XMLStreamException
	{
		writer.writeProcessingInstruction( target, data );
	}

	@Override
	public void writeCData( String data )
		throws XMLStreamException
	{
		writer.writeCData( data );
	}

	@Override
	public void writeDTD( String dtd )
		throws XMLStreamException
	{
		writer.writeDTD( dtd );
	}

	@Override
	public void writeEntityRef( String name )
		throws XMLStreamException
	{
		writer.writeEntityRef( name );
	}

	@Override
	public void writeStartDocument()
		throws XMLStreamException
	{
		writer.writeStartDocument();
	}

	@Override
	public void writeStartDocument( String version )
		throws XMLStreamException
	{
		writer.writeStartDocument( version );
	}

	@Override
	public void writeStartDocument( String encoding, String version )
		throws XMLStreamException
	{
		writer.writeStartDocument( encoding, version );
	}

	@Override
	public void writeCharacters( String text )
		throws XMLStreamException
	{
		writer.writeCharacters( text );
	}

	@Override
	public void writeCharacters( char[] text, int start, int len )
		throws XMLStreamException
	{
		writer.writeCharacters( text, start, len );
	}

	@Override
	public String getPrefix( String uri )
		throws XMLStreamException
	{
		return writer.getPrefix( uri );
	}

	@Override
	public void setPrefix( String prefix, String uri )
		throws XMLStreamException
	{
		writer.setPrefix( prefix, uri );
	}

	@Override
	public void setDefaultNamespace( String uri )
		throws XMLStreamException
	{
		writer.setDefaultNamespace( uri );
	}

	@Override
	public void setNamespaceContext( NamespaceContext context )
		throws XMLStreamException
	{
		writer.setNamespaceContext( context );
	}

	@Override
	public NamespaceContext getNamespaceContext()
	{
		return writer.getNamespaceContext();
	}

	@Override
	public Object getProperty( String name )
	{
		return writer.getProperty( name );
	}
}
