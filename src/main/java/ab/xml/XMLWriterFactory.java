/*
 * AsoBrain XML Library
 * Copyright (C) 1999-2011 Peter S. Heijnen
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

/**
 * Factory for creating {@link XMLWriter} instances using an XML API that is
 * available on the current platform.
 *
 * <p> The following APIs are currently supported: <ul>
 *
 * <li>The Streaming API for XML (StAX)</li>
 *
 * <li>XML Pull</li> </ul>
 *
 * For an overview of differences with these APIs, see {@link XMLWriter}.
 *
 * @author G. Meinders
 */
public abstract class XMLWriterFactory
{
	/**
	 * Class names of factory implementations.
	 */
	@SuppressWarnings( "SpellCheckingInspection" )
	private static final String[] FACTORY_CLASS_NAMES =
	{
	"ab.xml.XmlPullWriterFactory",
	"ab.xml.StaxWriterFactory"
	};

	/**
	 * Create a new factory that uses an XML API that is available on the
	 * current platform.
	 *
	 * @return Factory instance.
	 *
	 * @throws FactoryException if no factory can be loaded.
	 */
	public static XMLWriterFactory newInstance()
	{
		XMLWriterFactory result = null;

		for ( final String className : FACTORY_CLASS_NAMES )
		{
			try
			{
				//noinspection unchecked
				final Class<XMLWriterFactory> clazz = (Class<XMLWriterFactory>)Class.forName( className );
				//noinspection JavaReflectionMemberAccess
				result = clazz.getConstructor().newInstance();
				break;
			}
			catch ( final FactoryException e )
			{
				// Factory determined a problem. Use another factory.
			}
			catch ( final NoClassDefFoundError e )
			{
				// If the underlying API is not available.
			}
			catch ( final ClassNotFoundException e )
			{
				/*
				 * If the factory class doesn't exist.
				 * If so, the list of factory classes should be corrected.
				 */
				throw new FactoryException( e );
			}
			catch ( final IllegalAccessException e )
			{
				/*
				 * If the factory class doesn't have a public constructor.
				 * If so, the factory class should be corrected.
				 */
				throw new FactoryException( e );
			}
			catch ( final Throwable e )
			{
				/*
				 * Any other problem with the factory probably indicates a
				 * programming error as well.
				 */
				throw new FactoryException( e );
			}
		}

		if ( result == null )
		{
			throw new FactoryException( "Could not find an implementation that is supported by the current platform." );
		}

		return result;
	}

	/**
	 * Whether XML writers created by the factory should automatically indent
	 * the written XML. Indenting is off by default.
	 */
	private boolean _indenting;

	/**
	 * String inserted as a newline.
	 */
	private String _newline = "\n";

	/**
	 * String inserted for each level of indenting.
	 */
	private String _indent = "\t";

	/**
	 * Constructs a new factory instance.
	 */
	protected XMLWriterFactory()
	{
		_indenting = false;
	}

	/**
	 * Returns whether XML writers created by the factory should automatically
	 * indent the written XML. Indenting is disabled by default.
	 *
	 * @return {@code true} if indenting is enabled.
	 */
	public boolean isIndenting()
	{
		return _indenting;
	}

	/**
	 * Sets whether XML writers created by the factory should automatically
	 * indent the written XML. Indenting is disabled by default.
	 *
	 * @param indenting {@code true} to enable indenting.
	 */
	public void setIndenting( final boolean indenting )
	{
		_indenting = indenting;
	}

	/**
	 * Returns the string to be used as a newline. Only used when {@link
	 * #isIndenting()} is {@code true}. The default is {@code "\n"}.
	 *
	 * @return String for newlines.
	 */
	public String getNewline()
	{
		return _newline;
	}

	/**
	 * Sets the string to be used as a newline. Only used when {@link
	 * #isIndenting()} is {@code true}. The default is {@code "\n"}.
	 *
	 * @param newline String for newlines.
	 */
	public void setNewline( final String newline )
	{
		_newline = newline;
	}

	/**
	 * Returns the string to be used for indenting. Only used when {@link
	 * #isIndenting()} is {@code true}. The default is {@code "\t"}.
	 *
	 * @return String for indenting.
	 */
	public String getIndent()
	{
		return _indent;
	}

	/**
	 * Sets the string to be used for indenting. Only used when {@link
	 * #isIndenting()} is {@code true}. The default is {@code "\t"}.
	 *
	 * @param indent String for indenting.
	 */
	public void setIndent( final String indent )
	{
		_indent = indent;
	}

	/**
	 * Creates an XML writer.
	 *
	 * @param out      Stream to write to.
	 * @param encoding Character encoding to be used.
	 *
	 * @return Created XML writer.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	public abstract XMLWriter createXMLWriter( final OutputStream out, final String encoding )
	throws XMLException;

	/**
	 * Creates an XML writer.
	 *
	 * @param writer   Character stream to write to.
	 * @param encoding Character encoding to be used.
	 *
	 * @return Created XML writer.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	public abstract XMLWriter createXMLWriter( Writer writer, String encoding )
	throws XMLException;
}
