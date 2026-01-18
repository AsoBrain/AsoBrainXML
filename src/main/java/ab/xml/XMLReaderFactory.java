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

import lombok.*;
import org.jspecify.annotations.*;

/**
 * Factory for creating {@link XMLReader} instances using an XML API that is
 * available on the current platform.
 *
 * <p> The following APIs are currently supported: <ul>
 *
 * <li>Streaming API for XML (StAX)</li>
 *
 * <li>XML Pull</li> </ul>
 *
 * @author G. Meinders
 */
@NoArgsConstructor( access = AccessLevel.PROTECTED )
public abstract class XMLReaderFactory
{
	/**
	 * Class names of factory implementations.
	 */
	private static final String[] FACTORY_CLASS_NAMES =
		{
			"ab.xml.XmlPullReaderFactory",
			"ab.xml.StaxReaderFactory"
		};

	/**
	 * Create a new factory that uses an XML API that is available on the
	 * current platform.
	 *
	 * @return Factory instance.
	 *
	 * @throws FactoryException if no factory can be loaded.
	 */
	public static XMLReaderFactory newInstance()
	{
		for ( var className : FACTORY_CLASS_NAMES )
		{
			var factory = newFactory( className );
			if ( factory != null )
			{
				return factory;
			}
		}

		throw new FactoryException( "Could not find an implementation that is supported by the current platform." );

	}

	@SuppressWarnings( { "ErrorNotRethrown", "squid:S1181" } )
	private static @Nullable XMLReaderFactory newFactory( String className )
	{
		try
		{
			return ( (Class<XMLReaderFactory>)Class.forName( className ) ).getConstructor().newInstance();
		}
		catch ( FactoryException | NoClassDefFoundError e )
		{
			// FactoryException if factory determined a problem. Use another factory.
			// NoClassDefFoundError if the underlying API is not available.
			return null;
		}
		catch ( Throwable e )
		{
			// ClassNotFoundException if the factory class doesn't exist. If so, the list of factory classes should be corrected.
			// IllegalAccessException: If the factory class doesn't have a public constructor. If so, the factory class should be corrected.
			throw new FactoryException( e );
		}
	}

	/**
	 * Creates an XML reader.
	 *
	 * @param in       Stream to read from.
	 * @param encoding Character encoding to be used; {@code null} to detect
	 *                 automatically.
	 *
	 * @return Created XML reader.
	 *
	 * @throws XMLException if an XML-related exception occurs.
	 */
	public abstract XMLReader createXMLReader( InputStream in, @Nullable String encoding )
		throws XMLException;
}
