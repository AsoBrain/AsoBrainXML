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

import java.util.*;
import javax.xml.*;
import javax.xml.parsers.*;

import lombok.*;
import org.jspecify.annotations.*;
import org.w3c.dom.*;

/**
 * This {@link XMLWriter} builds a DOM model (fragment).
 *
 * @author  Peter S. Heijnen
 */
@SuppressWarnings( "WeakerAccess" )
public class DomXmlWriter
implements XMLWriter
{
	/**
	 * Written XML document.
	 */
	@Getter
	private @Nullable Document document;

	/**
	 * Current DOM node.
	 */
	private @Nullable Node node;

	/**
	 * {@code true} if the writer is currently writing an empty tag.
	 */
	private boolean empty = false;

	/**
	 * Namespace declarations that need to be added to the next start element.
	 */
	private final Deque<Map<String, String>> nsPrefixStack = new LinkedList<>();

	/**
	 * Construct writer for a new document. Use {@link #startDocument()} to
	 * start the document.
	 */
	public DomXmlWriter()
	{
		this( null, null );
	}

	/**
	 * Construct writer for an existing document. The writer starts at the given
	 * node.
	 *
	 * @param document Document being written.
	 * @param node     Node to start at.
	 */
	public DomXmlWriter( @Nullable Document document, @Nullable Node node )
	{
		this.document = document;
		this.node = node;
		nsPrefixStack.add( new LinkedHashMap<>() );
	}

	@Override
	public void startDocument()
	throws XMLException
	{
		if ( document != null )
		{
			throw new XMLException( "Can't start document. We already have a document." );
		}

		var dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware( true );

		DocumentBuilder db;
		try
		{
			db = dbf.newDocumentBuilder();
		}
		catch ( ParserConfigurationException e )
		{
			throw new XMLException( e );
		}

		document = db.newDocument();
		node = document;
	}

	@Override
	public void setPrefix( String prefix, String namespaceURI )
	{
		var nsPrefixes = nsPrefixStack.getLast();
		nsPrefixes.put( namespaceURI, prefix );
	}

	@Override
	public void startTag( String namespaceURI, String localName )
	throws XMLException
	{
		if ( empty )
		{
			throw new XMLException( "Not allowed inside an empty tag. Use 'endTag' first." );
		}

		if ( ( document == null ) || ( node == null ) )
		{
			throw new XMLException( "Can't start tag before the document is started" );
		}

		Element element;

		if ( namespaceURI != null )
		{
			element = document.createElementNS( namespaceURI, getQualifiedName( namespaceURI, localName ) );
		}
		else
		{
			element = document.createElementNS( XMLConstants.DEFAULT_NS_PREFIX, localName );
		}

		var nsPrefixes = nsPrefixStack.getLast();
		for ( var entry : nsPrefixes.entrySet() )
		{
			var namespaceUri = entry.getKey();
			var prefix = entry.getValue();
			//noinspection HttpUrlsUsage
			element.setAttributeNS( "http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, namespaceUri );
		}

		nsPrefixStack.add( new LinkedHashMap<>() );

		node.appendChild( element );
		node = element;
	}

	@Override
	public void emptyTag( String namespaceURI, String localName )
	throws XMLException
	{
		startTag( namespaceURI, localName );
		empty = true;
	}

	@Override
	public void attribute( String namespaceURI, String localName, String value )
	throws XMLException
	{
		if ( !( node instanceof Element element ) )
		{
			throw new XMLException( "Must start tag before setting attributes" );
		}

		if ( namespaceURI == null )
		{
			element.setAttributeNS( element.getNamespaceURI(), localName, value );
		}
		else
		{
			element.setAttributeNS( namespaceURI, getQualifiedName( namespaceURI, localName ), value );
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

		if ( ( document == null ) || ( node == null ) )
		{
			throw new XMLException( "Can't start tag before the document is started" );
		}

		var lastChild = node.getLastChild();
		if ( lastChild instanceof Text text )
		{
			text.appendData( characters );
		}
		else
		{
			node.appendChild( document.createTextNode( characters ) );
		}
	}

	@Override
	public void endTag( String namespaceURI, String localName )
	throws XMLException
	{
		empty = false;

		var nodeNamespaceURI = Objects.requireNonNull( node, "Must have node by now" ).getNamespaceURI();
		var nodeLocalName = node.getLocalName();

		if ( !localName.equals( nodeLocalName ) || ( !Objects.equals( namespaceURI, nodeNamespaceURI ) ) )
		{
			throw new XMLException( "Unbalanced end tag (<%s xml:ns=\"%s\"> vs </%s xml:ns=\"%s\">".formatted( getQualifiedName( nodeNamespaceURI, nodeLocalName ), nodeNamespaceURI,
			                                                                                                   getQualifiedName( namespaceURI, localName ), namespaceURI ) );
		}

		nsPrefixStack.removeLast();
		node = node.getParentNode();
	}

	@Override
	public void endDocument()
	throws XMLException
	{
		if ( ( document == null ) || ( node == null ) )
		{
			throw new XMLException( "Can't end document before the document is started" );
		}

		//noinspection ObjectEquality
		if ( node != document )
		{
			throw new XMLException( "Can't end document before the document is started" );
		}
	}

	@Override
	public void flush()
	{
		// nothing to do
	}

	/**
	 * Get qualified name for a given name space and locale name.
	 *
	 * @param namespaceURI Namespace URI of node/attribute.
	 * @param localName    Local name of the node/attribute.
	 *
	 * @return Qualified name based on namespace URI and known prefixes; {@code
	 * localName} if name could not be qualified.
	 */
	private String getQualifiedName( String namespaceURI, String localName )
	{
		for ( var it = nsPrefixStack.descendingIterator(); it.hasNext(); )
		{
			var nsPrefixes = it.next();
			var prefix = nsPrefixes.get( namespaceURI );
			if ( prefix != null )
			{
				return prefix + ':' + localName;
			}
		}

		return localName;
	}
}
