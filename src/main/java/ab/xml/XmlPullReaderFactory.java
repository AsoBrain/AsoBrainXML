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

import org.xmlpull.v1.*;

/**
 * Factory for XML readers that use XML Pull.
 *
 * @author G. Meinders
 */
class XmlPullReaderFactory
extends XMLReaderFactory
{
	/**
	 * Factory used to create XML Pull readers.
	 */
	private final XmlPullParserFactory factory;

	XmlPullReaderFactory()
	{
		try
		{
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware( true );
		}
		catch ( XmlPullParserException e )
		{
			throw new FactoryException( e );
		}
	}

	@Override
	public XMLReader createXMLReader( InputStream in, String encoding )
	throws XMLException
	{
		XmlPullParser parser;
		try
		{
			parser = factory.newPullParser();
			parser.setInput( in, encoding );
		}
		catch ( XmlPullParserException e )
		{
			throw new XMLException( e );
		}

		return new XmlPullReader( parser );
	}
}
