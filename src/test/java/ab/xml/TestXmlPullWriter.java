/*
 * AsoBrain XML Library
 * Copyright (C) 2019-2026 Peter S. Heijnen
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

import org.junit.jupiter.api.*;

/**
 * Unit test for {@link XmlPullWriter}.
 *
 * @author Gerrit Meinders
 */
class TestXmlPullWriter
{
	/**
	 * Tests that a simple document with namespaces can be written.
	 *
	 * @throws Exception if the test fails.
	 */
	@SuppressWarnings( "TestMethodWithoutAssertion" )
	@Test
	void testDocument1()
		throws Exception
	{
		var xmlWriterFactory = XMLWriterFactory.newInstance();
		xmlWriterFactory.setIndenting( true );

		var actual = new ByteArrayOutputStream();
		var charset = "UTF-8";
		var xmlWriter = xmlWriterFactory.createXMLWriter( actual, charset );

		var namespaceURI = "https://www.example.com/";
		xmlWriter.startDocument();
		xmlWriter.setPrefix( "ab", namespaceURI );
		xmlWriter.startTag( namespaceURI, "test" );
		{
			xmlWriter.emptyTag( namespaceURI, "hello" );
			xmlWriter.attribute( null, "type", "greeting" );
			xmlWriter.endTag( namespaceURI, "hello" );

			xmlWriter.emptyTag( namespaceURI, "world" );
			xmlWriter.endTag( namespaceURI, "world" );
		}
		xmlWriter.endTag( namespaceURI, "test" );
		xmlWriter.endDocument();

		XMLTestTools.assertXMLEquals( "Unexpected output", getClass().getResourceAsStream( "TestXmlPullWriter-1.xml" ), new ByteArrayInputStream( actual.toByteArray() ) );
	}
}
