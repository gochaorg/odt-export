package xyz.cofe.odtexport.odt.style

import org.w3c.dom.{Node => XMLNode};
import org.w3c.dom.{Element => XMLElement}
import xyz.cofe.odtexport.odt.Export
;

/**
 * Описывает набор стилей ODT (content.xml) документа.
 * @param odtContent Узел дерева XML файла content.xml (ODT)
 */
class Styles( odtContent : XMLNode )
{
	import collection.immutable.Map;
	import xyz.cofe.odtexport.XMLNode._;

	/**
	* Возвращает корень древа XML
	* @param node Узел дерева
	* @return Корень дерева
	*/
	private def rootNode( node:XMLNode ):XMLNode = {
		if( node==null )return node;
		var c = node;
		while( c.getParentNode!=null ) c = c.getParentNode;
		c
	}

	/**
	 * Создает карту свойств стилей
	 */
	private def buildStylePropertyMap(rootN:XMLNode):Map[
			String, // Стиль
			List[Property] // Свойства стиля
		]={
    if( Export.verbose )println( "buildStylePropertyMap() begin" );

        def build(n:XMLNode,parent:Property = null):List[Property] = {
            var r = List[Property]();

            if( n.hasAttributes ){
                for( i <- 0 until n.getAttributes.getLength ){
                    val attr = n.getAttributes.item(i);
                    val aName = attr.getNodeName;
                    val aVal = attr.getTextContent;
                    val s = new Property( aName,aVal );
                    r = s :: r;
	                if( Export.verbose )println( "propery "+aName+"="+aVal );
                }
            }

            if( n.hasChildNodes ){
                for( c <- n.getChildNodes ){
                    if( c.isInstanceOf[XMLElement] ){
                        val name = c.getNodeName;
                        if( name!=null ){
                            val s = new Property(name);

                            val cvl = build(c,s);
                            for( cv <- cvl )s.add(cv);

                            r = s :: r;
                        }
                    }
                }
            }

            r;
        }

        var styleMap = Map[String,List[Property]]();

		for( node <- rootN.xpathNodes("*//style[@name]") ) {
            if( node.isInstanceOf[XMLElement] ){
                val el = node.asInstanceOf[XMLElement];
                val name = el.getAttribute("style:name");

	            if( Export.verbose )println("style name="+name);
                styleMap = styleMap + ((name,build(node)));
            }
		}

		for( node <- rootN.xpathNodes("*//list-style[@name]") ) {
            if( node.isInstanceOf[XMLElement] ){
                val el = node.asInstanceOf[XMLElement];
                val name = el.getAttribute("style:name");

	            if( Export.verbose )println("style name="+name);
                styleMap = styleMap + ((name,build(node)));
            }
		}

        if( Export.verbose )println( "buildStylePropertyMap() end" );

		styleMap;
	}

	lazy val stylePropertiesMap : Map[
			String, // Стиль
			List[Property] // Свойства стиля
	] =  buildStylePropertyMap( rootNode(odtContent) );

	/**
	 * Карта иерархии стилей
	 */
	lazy val styleHierarchy : Map[
			String, //Имя дочернего стиля
			String  //Имя родительского стиля
	] = {
		import xyz.cofe.odtexport.XMLNode._;
		var res = Map[String,String]();
		for( node <- rootNode(odtContent).xpathNodes("*//style[@name and @parent-style-name]") )
		{
			val styleName = node.getAttributes.getNamedItem("style:name").getTextContent;
			val parentStyleName = node.getAttributes.getNamedItem("style:parent-style-name").getTextContent;
			res = res + ((styleName,parentStyleName));
		}
		res
	};

	/**
	* Проверяет является-ли указанный стиль дочерним по отношению к указанному
	* @param child Имя дочернего стиля
	* @param parent Имя родительского стиля
	* @return true/false - является/не является дочерним стилем.
	*/
	def isChildOfStyle( child:String, parent:String ):Boolean = {
		var res = false;
		var current = child;
		var stop = false;
		var path = List[String](current); // Путь для проверки, циклов
		while( !stop ){
			if( styleHierarchy.contains(current) ){
				val newCurrent = styleHierarchy(current);
				if( path.contains(newCurrent) ){ //Начался цикл
					stop = true;
				}else{
					path = newCurrent :: path;
					current = newCurrent;
					if( current.equals(parent) ){
						res = true;
						stop = true;
					}
				}
			}else{
				stop = true;
			}
		}
		res
	}

	/**
	 * Возвращает имя стиля (кодированное)
	 * @param node Узел
	 * @return Имя стиля или null если стиль не указан (нет значения у атрибутта text:style-name / draw:style-name)
	 */
	def getStyleNameOf( node: XMLNode ):String = {
        if( node==null )return throw new IllegalArgumentException();

        if( node.hasAttributes ){
            var a =  node.getAttributes.getNamedItem("text:style-name");

            if( a == null )
                a = node.getAttributes.getNamedItem("draw:style-name");

            if( a!=null )
            {
                val attr = a.getTextContent;
                return attr;
            }
        }
	    null
	}

	/**
	 * Возвразает ствойства указанного стиля
	 */
	def getStyleProperties(styleName:String):List[Property] = {
	    if( styleName==null ) throw new IllegalArgumentException();

	    if( stylePropertiesMap.contains(styleName) ){
	        return stylePropertiesMap(styleName);
	    }

		null
	}
}