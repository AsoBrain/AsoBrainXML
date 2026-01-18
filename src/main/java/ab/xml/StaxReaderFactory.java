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

import lombok.extern.slf4j.*;

/**
 * Factory for XML readers that use StAX, the Streaming API for XML.
 *
 * @author G. Meinders
 */
@Slf4j
class StaxReaderFactory
	extends XMLReaderFactory
{
	/**
	 * Factory used to create StAX readers.
	 */
	private final XMLInputFactory factory;

	StaxReaderFactory()
	{
		factory = newFactory();
		factory.setProperty( XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE );
		factory.setProperty( XMLInputFactory.IS_COALESCING, Boolean.TRUE );
		factory.setProperty( XMLInputFactory.SUPPORT_DTD, Boolean.FALSE );
	}

	@SuppressWarnings( "ErrorNotRethrown" )
	private XMLInputFactory newFactory()
	{
		try
		{
			// NOTE: For OpenJDK, this requires a file 'META-INF/services/com.sun.xml.internal.stream.XMLInputFactoryImpl' containing the factory class name.
			return XMLInputFactory.newFactory( "com.sun.xml.internal.stream.XMLInputFactoryImpl", getClass().getClassLoader() );
		}
		catch ( FactoryConfigurationError ignored )
		{
			var defaultFactory = XMLInputFactory.newFactory();
			Class<?> factoryClass = defaultFactory.getClass();
			log.atWarn().log( () -> "%s: Using StAX factory class %s, which is not the recommended implementation.".formatted( getClass().getName(), factoryClass.getName() ) );
			return defaultFactory;
		}
	}

	@Override
	public XMLReader createXMLReader( InputStream in, String encoding )
		throws XMLException
	{
		try
		{
			return new StaxReader( factory.createXMLStreamReader( in, encoding ) );
		}
		catch ( XMLStreamException e )
		{
			throw new XMLException( e );
		}

	}
}
