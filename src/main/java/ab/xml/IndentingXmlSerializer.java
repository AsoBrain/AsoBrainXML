/*
 * AsoBrain XML Library
 * Copyright (C) 1999-2019 Peter S. Heijnen
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

import org.jetbrains.annotations.*;
import org.xmlpull.v1.*;

/**
 * Wraps an underlying {@link XmlSerializer} to automatically add newlines and
 * indenting for elements. Elements that do no contain nested elements are
 * themselves indented, but have no newlines and indenting added around their
 * content.
 *
 * @author G. Meinders
 */
public class IndentingXmlSerializer
implements XmlSerializer
{
	/**
	 * Underlying writer.
	 */
	@NotNull
	private final XmlSerializer _writer;

	/**
	 * Current nesting depth, used for indenting.
	 */
	private int _depth = 0;

	/**
	 * String inserted as a newline. The default is <code>"\n"</code>.
	 */
	@NotNull
	private String _newline = "\n";

	/**
	 * String inserted for each level of indenting. The default is
	 * <code>"\t"</code>.
	 */
	@NotNull
	private String _indent = "\t";

	/**
	 * Whether the current node contains no elements (like an
	 * 'xsd:simpleType').
	 */
	private boolean _simpleType = true;

	/**
	 * List of prefixes to set. For compatibility with the xpp3 implementation
	 * of {@link XmlSerializer}, prefixes have to be set immediately before
	 * {@link XmlSerializer#startTag} (i.e. after indentation).
	 */
	private final List<QName> _setPrefix = new ArrayList<QName>();

	/**
	 * Constructs a new indenting writer.
	 *
	 * @param writer Underlying writer.
	 */
	public IndentingXmlSerializer( @NotNull final XmlSerializer writer )
	{
		_writer = writer;
	}

	@SuppressWarnings( "WeakerAccess" )
	@NotNull
	public String getNewline()
	{
		return _newline;
	}

	public void setNewline( @NotNull final String newline )
	{
		_newline = newline;
	}

	@SuppressWarnings( "WeakerAccess" )
	@NotNull
	public String getIndent()
	{
		return _indent;
	}

	public void setIndent( @NotNull final String indent )
	{
		_indent = indent;
	}

	/**
	 * Starts a new line with the proper amount of indenting and increases the
	 * nesting depth.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void indentIn()
	throws IOException
	{
		_writer.text( getNewline() );
		final int depth = _depth++;
		for ( int i = 0; i < depth; i++ )
		{
			_writer.text( getIndent() );
		}
		_simpleType = true;
	}

	/**
	 * Decreases the nesting depth, and starts a new line with the proper amount
	 * of indenting (except for a {@link #_simpleType}).
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void indentOut()
	throws IOException
	{
		final int depth = --_depth;
		if ( !_simpleType )
		{
			_writer.text( getNewline() );
			for ( int i = 0; i < depth; i++ )
			{
				_writer.text( getIndent() );
			}
		}
		_simpleType = false;
	}

	@NotNull
	@Override
	public XmlSerializer startTag( final String namespace, final String name )
	throws IOException
	{
		indentIn();
		for ( final QName entry : _setPrefix )
		{
			_writer.setPrefix( entry.getPrefix(), entry.getNamespaceURI() );
		}
		_setPrefix.clear();
		_writer.startTag( namespace, name );
		return this;
	}

	@NotNull
	@Override
	public XmlSerializer endTag( final String namespace, final String name )
	throws IOException
	{
		indentOut();
		_writer.endTag( namespace, name );
		return this;
	}

	@Override
	public void endDocument()
	throws IOException
	{
		_writer.endDocument();
	}

	@Override
	public void flush()
	throws IOException
	{
		_writer.flush();
	}

	@NotNull
	@Override
	public XmlSerializer attribute( final String namespace, final String name, final String value )
	throws IOException
	{
		_writer.attribute( namespace, name, value );
		return this;
	}

	@Override
	public void setFeature( final String name, final boolean state )
	{
		_writer.setFeature( name, state );
	}

	@Override
	public boolean getFeature( final String name )
	{
		return _writer.getFeature( name );
	}

	@Override
	public void setProperty( @NotNull final String name, @Nullable final Object value )
	{
		_writer.setProperty( name, value );
	}

	@Nullable
	@Override
	public Object getProperty( @NotNull final String name )
	{
		return _writer.getProperty( name );
	}

	@Override
	public void setOutput( @NotNull final OutputStream os, @Nullable final String encoding )
	throws IOException
	{
		_writer.setOutput( os, encoding );
	}

	@Override
	public void setOutput( @NotNull final Writer writer )
	throws IOException
	{
		_writer.setOutput( writer );
	}

	@Override
	public void startDocument( @Nullable final String encoding, @Nullable final Boolean standalone )
	throws IOException
	{
		_writer.startDocument( encoding, standalone );
	}

	@Override
	public void setPrefix( @NotNull final String prefix, @NotNull final String namespace )
	{
		_setPrefix.add( new QName( namespace, "", prefix ) );
	}

	@Contract( "_, true -> !null; _, false -> _" )
	@Override
	public String getPrefix( @NotNull final String namespace, final boolean generatePrefix )
	{
		String result = null;
		for ( final QName entry : _setPrefix )
		{
			if ( namespace.equals( entry.getNamespaceURI() ) )
			{
				result = entry.getPrefix();
			}
		}
		if ( result == null )
		{
			result = _writer.getPrefix( namespace, generatePrefix );
		}
		return result;
	}

	@Override
	public int getDepth()
	{
		return _writer.getDepth();
	}

	@Nullable
	@Override
	public String getNamespace()
	{
		return _writer.getNamespace();
	}

	@Nullable
	@Override
	public String getName()
	{
		return _writer.getName();
	}

	@NotNull
	@Override
	public XmlSerializer text( @NotNull final String text )
	throws IOException
	{
		return _writer.text( text );
	}

	@NotNull
	@Override
	public XmlSerializer text( @NotNull final char[] buf, final int start, final int len )
	throws IOException
	{
		return _writer.text( buf, start, len );
	}

	@Override
	public void cdsect( @NotNull final String text )
	throws IOException
	{
		_writer.cdsect( text );
	}

	@Override
	public void entityRef( @NotNull final String text )
	throws IOException
	{
		_writer.entityRef( text );
	}

	@Override
	public void processingInstruction( @NotNull final String text )
	throws IOException
	{
		_writer.processingInstruction( text );
	}

	@Override
	public void comment( @NotNull final String text )
	throws IOException
	{
		_writer.comment( text );
	}

	@Override
	public void docdecl( @NotNull final String text )
	throws IOException
	{
		_writer.docdecl( text );
	}

	@Override
	public void ignorableWhitespace( @NotNull final String text )
	throws IOException
	{
		_writer.ignorableWhitespace( text );
	}
}
