/* 
 * The MIT License
 *
 * Copyright 2014 Kamnev Georgiy (nt.gocha@gmail.com).
 *
 * Данная лицензия разрешает, безвозмездно, лицам, получившим копию данного программного 
 * обеспечения и сопутствующей документации (в дальнейшем именуемыми "Программное Обеспечение"), 
 * использовать Программное Обеспечение без ограничений, включая неограниченное право на 
 * использование, копирование, изменение, объединение, публикацию, распространение, сублицензирование 
 * и/или продажу копий Программного Обеспечения, также как и лицам, которым предоставляется 
 * данное Программное Обеспечение, при соблюдении следующих условий:
 *
 * Вышеупомянутый копирайт и данные условия должны быть включены во все копии 
 * или значимые части данного Программного Обеспечения.
 *
 * ДАННОЕ ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ ПРЕДОСТАВЛЯЕТСЯ «КАК ЕСТЬ», БЕЗ ЛЮБОГО ВИДА ГАРАНТИЙ, 
 * ЯВНО ВЫРАЖЕННЫХ ИЛИ ПОДРАЗУМЕВАЕМЫХ, ВКЛЮЧАЯ, НО НЕ ОГРАНИЧИВАЯСЬ ГАРАНТИЯМИ ТОВАРНОЙ ПРИГОДНОСТИ, 
 * СООТВЕТСТВИЯ ПО ЕГО КОНКРЕТНОМУ НАЗНАЧЕНИЮ И НЕНАРУШЕНИЯ ПРАВ. НИ В КАКОМ СЛУЧАЕ АВТОРЫ 
 * ИЛИ ПРАВООБЛАДАТЕЛИ НЕ НЕСУТ ОТВЕТСТВЕННОСТИ ПО ИСКАМ О ВОЗМЕЩЕНИИ УЩЕРБА, УБЫТКОВ 
 * ИЛИ ДРУГИХ ТРЕБОВАНИЙ ПО ДЕЙСТВУЮЩИМ КОНТРАКТАМ, ДЕЛИКТАМ ИЛИ ИНОМУ, ВОЗНИКШИМ ИЗ, ИМЕЮЩИМ 
 * ПРИЧИНОЙ ИЛИ СВЯЗАННЫМ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ ИЛИ ИСПОЛЬЗОВАНИЕМ ПРОГРАММНОГО ОБЕСПЕЧЕНИЯ 
 * ИЛИ ИНЫМИ ДЕЙСТВИЯМИ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ.
 */

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