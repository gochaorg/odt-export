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

import xyz.cofe.odtexport.xtest.XTest;
import org.w3c.dom.{Node => XMLNode};
import xyz.cofe.odtexport.odt.style.Property;
import xyz.cofe.odtexport.odt.style.Styles;

/**
 * Набор условий для проверки свойств "стиля"
 * @param Имя свойства
 */
class StyleProperty( styles: =>Styles, val name:String* ) {

    private def getPropertyByName( name:String, props:List[Property] ):List[Property] = {
        var res : List[Property] = List();
        for( p <- props ){
            if( p.name.equals(name) )res = p :: res;
        }
        res
    }

    private def getChildrenByName( name:String, props:List[Property] ):List[Property] = {
        var res : List[Property] = List();
        for( p <- props ){
            for( pc <- p.children ){
                if( pc.name.equals(name) )res = pc :: res;
            }
        }
        res
    }

    private def printProps( props:List[Property] ):Unit = {
        for( p <- props ){
            println( p.toString );
        }
    }

    private def getProperty( props:List[Property] ):List[Property] = {
        if( name.length==0 ){
            return null;
        }

        var current : List[Property] = props;
        current = getPropertyByName( name(0), current );

        for( i <- 1 until name.length ){
            current = getChildrenByName( name(i), current );
        }

        current
    }

    private def getPropertyValue( props:List[Property] ) : String = {
        val v = getProperty( props );
        if( v!=null ){
            if( v.length>0 ){
                return v(0).value;
            }
        }
        null
    }

    private def getPropertyValue( node: XMLNode ) : String = {
        if( node==null )return null;

        val styleName = styles.getStyleNameOf(node);

        if( styleName != null ) {
            val props = styles.getStyleProperties(styleName);
            if( props != null ) {
                return getPropertyValue( props );
            }
        }

        null
    }

	/**
	 * Значение указанного свойства стиля совпадает с переданным значением
	 * @param value Искомое значение
	 * @return Проверка на совпадение свойства с искоммым значением
	 */
	def == (value:String) : XTest = {
		new XTest() {
			override def test( node: XMLNode ):Boolean = {
                val stylePropValue = getPropertyValue( node );

                /*print("SP");
                for( n <- name )print( "["+n+"]" );
                println( "="+stylePropValue+" , need="+value );*/

                return if( stylePropValue==null ){
                    value==null;
                }else{
                    if( value==null )
                        false
                    else
                        stylePropValue.equals(value);
                }
			}
		}
	}

	/**
	 * Проверяет наличие указанного свойства
	 * @return Проверка наличии указанного свойства
	 */
	def exists : XTest = {
		new XTest() {
			override def test( node: XMLNode ):Boolean = {
		        val styleName = styles.getStyleNameOf(node);
		        if( styleName == null )return false;

	            val props = styles.getStyleProperties(styleName);
				if( props==null )return false;

	            val nprops = getProperty( props );
	            return nprops.size > 0;
			}
		}
	}
}