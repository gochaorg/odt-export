package xyz.cofe.odtexport.odt.xtest

import org.w3c.dom.{Node => XMLNode};

import xyz.cofe.odtexport.xtest._;
import xyz.cofe.odtexport.odt.style.Styles;

/**
 * Условие совпадения названия "стиля"
 * @param name Имя стиля
 * @param encodeName Имя стиля передано в не кодированном состоянии (по умолч. true)
 */
class StyleName( styles: =>Styles, val name:String, val encodeName:Boolean=true ) extends XTest
{
    private lazy val encodedName : String = {
        if( encodeName ){
            val sb = new StringBuilder();
            for( idx <- 0 until name.length ){
                val c = name.charAt(idx);

                if( c.isDigit ) sb.append( c );
                else if( c.isLetter )sb.append( c );
                else {
                    sb.append( c.toByte.formatted("_%h_") );
                }
            }
            sb.toString();
        }else name
    }

	override def test( node:XMLNode ):Boolean = {
		val nodeStyle = styles.getStyleNameOf( node );
		if( nodeStyle!=null )
		{
			if( nodeStyle.equals(encodedName) )return true;
			if( styles.isChildOfStyle(nodeStyle, encodedName) )return true;
		}
		return false;
	}
}