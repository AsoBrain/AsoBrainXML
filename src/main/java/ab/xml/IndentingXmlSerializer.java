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
import javax.xml.namespace.*;

import lombok.*;
import org.jspecify.annotations.*;
import org.xmlpull.v1.*;

/**
 * Wraps an underlying {@link XmlSerializer} to automatically add newlines and
 * indenting for elements. Elements that do no contain nested elements are
 * themselves indented, but have no newlines and indenting added around their
 * content.
 *
 * @author G. Meinders
 */
@RequiredArgsConstructor
@SuppressWarnings( "unused" )
public class IndentingXmlSerializer
	implements XmlSerializer
{
	/**
	 * Underlying writer.
	 */
	private final XmlSerializer writer;

	/**
	 * Current nesting depth, used for indenting.
	 */
	private int depth = 0;

	/**
	 * String inserted as a newline. The default is {@code "\n"}.
	 */
	@Getter
	@Setter
	private String newline = "\n";

	/**
	 * String inserted for each level of indenting. The default is
	 * {@code "\t"}.
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
	 * List of prefixes to set. For compatibility with the xpp3 implementation
	 * of {@link XmlSerializer}, prefixes have to be set immediately before
	 * {@link XmlSerializer#startTag} (i.e. after indentation).
	 */
	private final List<QName> setPrefix = new ArrayList<>();

	/**
	 * Starts a new line with the proper amount of indenting and increases the
	 * nesting depth.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void indentIn()
		throws IOException
	{
		writer.text( getNewline() );
		var curDepth = depth++;
		for ( var i = 0; i < curDepth; i++ )
		{
			writer.text( getIndent() );
		}
		simpleType = true;
	}

	/**
	 * Decreases the nesting depth, and starts a new line with the proper amount
	 * of indenting (except for a {@link #simpleType}).
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void indentOut()
		throws IOException
	{
		var curDepth = --depth;
		if ( !simpleType )
		{
			writer.text( getNewline() );
			for ( var i = 0; i < curDepth; i++ )
			{
				writer.text( getIndent() );
			}
		}
		simpleType = false;
	}

	@Override
	public XmlSerializer startTag( String namespace, String name )
		throws IOException
	{
		indentIn();
		for ( var entry : setPrefix )
		{
			writer.setPrefix( entry.getPrefix(), entry.getNamespaceURI() );
		}
		setPrefix.clear();
		writer.startTag( namespace, name );
		return this;
	}

	@Override
	public XmlSerializer endTag( String namespace, String name )
		throws IOException
	{
		indentOut();
		writer.endTag( namespace, name );
		return this;
	}

	@Override
	public void endDocument()
		throws IOException
	{
		writer.endDocument();
	}

	@Override
	public void flush()
		throws IOException
	{
		writer.flush();
	}

	@Override
	public XmlSerializer attribute( String namespace, String name, String value )
		throws IOException
	{
		writer.attribute( namespace, name, value );
		return this;
	}

	@Override
	public void setFeature( String name, boolean state )
	{
		writer.setFeature( name, state );
	}

	@Override
	public boolean getFeature( String name )
	{
		return writer.getFeature( name );
	}

	@Override
	public void setProperty( String name, @Nullable Object value )
	{
		writer.setProperty( name, value );
	}

	@Override
	public @Nullable Object getProperty( String name )
	{
		return writer.getProperty( name );
	}

	@Override
	public void setOutput( OutputStream os, @Nullable String encoding )
		throws IOException
	{
		writer.setOutput( os, encoding );
	}

	@Override
	public void setOutput( Writer writer )
		throws IOException
	{
		this.writer.setOutput( writer );
	}

	@Override
	public void startDocument( @Nullable String encoding, @Nullable Boolean standalone )
		throws IOException
	{
		writer.startDocument( encoding, standalone );
	}

	@Override
	public void setPrefix( String prefix, String namespace )
	{
		setPrefix.add( new QName( namespace, "", prefix ) );
	}

	//@Contract( "_, true -> !null; _, false -> _" )
	@Override
	public String getPrefix( String namespace, boolean generatePrefix )
	{
		for ( var entry : setPrefix )
		{
			if ( namespace.equals( entry.getNamespaceURI() ) )
			{
				return entry.getPrefix();
			}
		}
		return writer.getPrefix( namespace, generatePrefix );
	}

	@Override
	public int getDepth()
	{
		return writer.getDepth();
	}

	@Override
	public @Nullable String getNamespace()
	{
		return writer.getNamespace();
	}

	@Override
	public @Nullable String getName()
	{
		return writer.getName();
	}

	@Override
	public XmlSerializer text( String text )
		throws IOException
	{
		return writer.text( text );
	}

	@Override
	public XmlSerializer text( char[] buf, int start, int len )
		throws IOException
	{
		return writer.text( buf, start, len );
	}

	@Override
	public void cdsect( String text )
		throws IOException
	{
		writer.cdsect( text );
	}

	@Override
	public void entityRef( String text )
		throws IOException
	{
		writer.entityRef( text );
	}

	@Override
	public void processingInstruction( String text )
		throws IOException
	{
		writer.processingInstruction( text );
	}

	@Override
	public void comment( String text )
		throws IOException
	{
		writer.comment( text );
	}

	@Override
	public void docdecl( String text )
		throws IOException
	{
		writer.docdecl( text );
	}

	@Override
	public void ignorableWhitespace( String text )
		throws IOException
	{
		writer.ignorableWhitespace( text );
	}
}
